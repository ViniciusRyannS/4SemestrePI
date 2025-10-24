package br.com.julio.pi.backoffice_users.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.julio.pi.backoffice_users.dto.cliente.AlterarSenhaDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.ClienteRegisterDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.ClienteUpdateDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.EnderecoCreateDTO;
import br.com.julio.pi.backoffice_users.model.cliente.Cliente;
import br.com.julio.pi.backoffice_users.model.cliente.EnderecoCliente;
import br.com.julio.pi.backoffice_users.model.cliente.Genero;
import br.com.julio.pi.backoffice_users.model.cliente.TipoEndereco;
import br.com.julio.pi.backoffice_users.repository.ClienteRepository;
import br.com.julio.pi.backoffice_users.repository.EnderecoClienteRepository;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clientes;
    private final EnderecoClienteRepository enderecos;
    private final SenhaService senhaService;
    private final CepService cepService;

    public ClienteService(ClienteRepository clientes,
                          EnderecoClienteRepository enderecos,
                          SenhaService senhaService,
                          CepService cepService) {
        this.clientes = clientes;
        this.enderecos = enderecos;
        this.senhaService = senhaService;
        this.cepService = cepService;
    }

    // ---------- Cadastro ----------
    @Transactional
    public Cliente registrar(ClienteRegisterDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dados obrigatórios");

        final String email = normalizeEmail(dto.email());
        final String cpf   = normalizeCpf(dto.cpf());

        if (email == null || email.isBlank()) throw new IllegalArgumentException("E-mail é obrigatório");
        if (cpf == null || cpf.isBlank())     throw new IllegalArgumentException("CPF é obrigatório");

        if (clientes.existsByEmail(email)) throw new IllegalArgumentException("E-mail já cadastrado");
        if (clientes.existsByCpf(cpf))     throw new IllegalArgumentException("CPF já cadastrado");

        if (dto.senha() == null || dto.senha().length() < 6) throw new IllegalArgumentException("Senha muito curta");
        if (!dto.senha().equals(dto.confirmaSenha())) throw new IllegalArgumentException("Senhas não conferem");

        Cliente c = new Cliente();
        c.setNomeCompleto(dto.nomeCompleto());
        c.setEmail(email);
        c.setCpf(cpf);
        c.setDataNascimento(dto.dataNascimento());
        c.setGenero(parseGenero(dto.genero()).orElse(null));
        c.setSenha(senhaService.gerarHash(dto.senha()));

        // FATURAMENTO (obrigatório)
        EnderecoCliente fat = montarEndereco(c, TipoEndereco.FATURAMENTO,
                dto.fatCep(), dto.fatNumero(), dto.fatComplemento(), false);
        c.getEnderecos().add(fat);

        // ENTREGA (usa fornecido ou copia FATURAMENTO)
        String entCep = dto.entCep() != null && !dto.entCep().isBlank() ? dto.entCep() : dto.fatCep();
        String entNum = dto.entNumero() != null && !dto.entNumero().isBlank() ? dto.entNumero() : dto.fatNumero();
        String entCmp = dto.entComplemento() != null ? dto.entComplemento() : dto.fatComplemento();
        EnderecoCliente ent = montarEndereco(c, TipoEndereco.ENTREGA, entCep, entNum, entCmp, true);
        c.getEnderecos().add(ent);

        log.info("Registrando cliente email={} cpf={}", email, maskCpf(cpf));
        Cliente salvo = clientes.save(c);

        // Defensivo: garante unicidade do "padrao" de ENTREGA
        garantirApenasUmPadraoEntrega(salvo);

        log.info("Cliente id={} registrado com sucesso", salvo.getId());
        return salvo;
    }

    private EnderecoCliente montarEndereco(Cliente c,
                                           TipoEndereco tipo,
                                           String cepRaw,
                                           String numero,
                                           String comp,
                                           boolean padraoEntrega) {

        String cep = normalizeCep(cepRaw);
        EnderecoCliente e = new EnderecoCliente();
        e.setCliente(c);
        e.setTipo(tipo);
        e.setPadrao(tipo == TipoEndereco.ENTREGA && padraoEntrega);

        // Consulta CEP (tolerante)
        if (cep != null && cep.length() == 8) {
            cepService.consultar(cep).ifPresent(v -> {
                if (isBlank(e.getLogradouro())) e.setLogradouro(nullSafe(v.logradouro()));
                if (isBlank(e.getBairro()))     e.setBairro(nullSafe(v.bairro()));
                if (isBlank(e.getCidade()))     e.setCidade(nullSafe(v.localidade()));
                if (isBlank(e.getUf()))         e.setUf(nullSafe(v.uf()));
            });
        }

        e.setCep(cep);
        e.setNumero(nullSafe(numero));
        e.setComplemento(nullSafe(comp));
        return e;
    }

    // ---------- Login ----------
    @Transactional(readOnly = true)
    public Cliente login(String emailRaw, String senhaPura) {
        final String email = normalizeEmail(emailRaw);
        var c = clientes.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário/senha inválidos"));
        if (!senhaService.verificar(senhaPura, c.getSenha()))
            throw new IllegalArgumentException("Usuário/senha inválidos");
        return c;
    }

    // ---------- Perfil ----------
    @Transactional(readOnly = true)
    public Cliente me(Long idCliente) {
        return clientes.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
    }

    @Transactional
    public Cliente atualizarBasico(Long id, ClienteUpdateDTO dto) {
        var c = me(id);
        if (dto.nomeCompleto() != null) c.setNomeCompleto(dto.nomeCompleto());
        if (dto.dataNascimento() != null) c.setDataNascimento(dto.dataNascimento());
        if (dto.genero() != null) c.setGenero(parseGenero(dto.genero()).orElse(null));
        return clientes.save(c);
    }

    @Transactional
public void alterarSenha(Long id, AlterarSenhaDTO dto) {
    var c = me(id);

    final String atual = dto.senhaAtual() == null ? "" : dto.senhaAtual();
    final String nova  = dto.novaSenha()  == null ? "" : dto.novaSenha().trim();

    // 1) Confere senha atual
    if (!senhaService.verificar(atual, c.getSenha())) {
        throw new IllegalArgumentException("Senha atual incorreta");
    }

    // 2) Regras da nova senha
    if (nova.length() < 6) {
        throw new IllegalArgumentException("Nova senha muito curta (mínimo 6 caracteres)");
    }
    if (nova.contains(" ")) {
        throw new IllegalArgumentException("Nova senha não pode conter espaços");
    }
    if (senhaService.verificar(nova, c.getSenha())) {
        throw new IllegalArgumentException("Nova senha não pode ser igual à senha atual");
    }
    if (c.getEmail() != null && nova.equalsIgnoreCase(c.getEmail())) {
        throw new IllegalArgumentException("Nova senha não pode ser igual ao e-mail");
    }
    if (c.getCpf() != null && nova.equals(c.getCpf())) {
        throw new IllegalArgumentException("Nova senha não pode ser igual ao CPF");
    }

    // 3) Persiste
    c.setSenha(senhaService.gerarHash(nova));
    clientes.save(c);
}


    // ---------- Endereços ----------
    @Transactional(readOnly = true)
    public List<EnderecoCliente> listarEnderecos(Long idCliente) {
        return enderecos.findByClienteIdOrderByIdDesc(idCliente);
    }

    @Transactional
public EnderecoCliente adicionarEndereco(Long idCliente, EnderecoCreateDTO dto) {
    var c = me(idCliente);

    EnderecoCliente e = montarEndereco(
        c,
        dto.tipo(),
        dto.cep(),
        dto.numero(),
        dto.complemento(),
        dto.tipo() == TipoEndereco.ENTREGA && Boolean.TRUE.equals(dto.padrao())
    );

    // vincula e salva explicitamente o filho (defensivo contra falta de cascade)
    e.setCliente(c);
    EnderecoCliente salvo = enderecos.save(e);

    // se marcou ENTREGA como padrão, garante unicidade
    if (Boolean.TRUE.equals(salvo.isPadrao()) && salvo.getTipo() == TipoEndereco.ENTREGA) {
        garantirApenasUmPadraoEntrega(c, salvo.getId());
        clientes.save(c); // flush no pai após ajustar flags
    }

    // retorna o recém-salvo
    return salvo;
}


    @Transactional
    public void setPadrao(Long idCliente, Long idEndereco, boolean padrao) {
        var c = me(idCliente);
        c.getEnderecos().forEach(e -> {
            if (Objects.equals(e.getId(), idEndereco)) {
                e.setPadrao(padrao);
            }
        });
        clientes.save(c);
        if (padrao) {
            garantirApenasUmPadraoEntrega(c, idEndereco);
            clientes.save(c);
        }
    }

    @Transactional
    public void removerEndereco(Long idCliente, Long idEndereco) {
        var c = me(idCliente);
        boolean removido = c.getEnderecos().removeIf(e -> Objects.equals(e.getId(), idEndereco));
        if (removido) {
            clientes.save(c);
        }
    }

    // ---------- Helpers de consistência ----------
    /** Garante que exista no máximo um endereço de ENTREGA marcado como padrão. */
    private void garantirApenasUmPadraoEntrega(Cliente c) {
        // Mantém o primeiro ENTREGA=true e desmarca os demais
        Long keepId = null;
        for (EnderecoCliente e : c.getEnderecos()) {
            if (e.getTipo() == TipoEndereco.ENTREGA && Boolean.TRUE.equals(e.isPadrao())) {
                if (keepId == null) {
                    keepId = e.getId();
                } else {
                    e.setPadrao(false);
                }
            }
        }
    }

    /** Desmarca todos os outros endereços de ENTREGA como padrão, mantendo apenas o id informado. */
    private void garantirApenasUmPadraoEntrega(Cliente c, Long manterId) {
        c.getEnderecos().forEach(e -> {
            if (e.getTipo() == TipoEndereco.ENTREGA && !Objects.equals(e.getId(), manterId)) {
                e.setPadrao(false);
            }
        });
    }

    // ---------- Utils ----------
    private static String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private static String normalizeCep(String cep) {
        if (cep == null) return null;
        String onlyDigits = cep.replaceAll("\\D", "");
        return onlyDigits.isBlank() ? null : onlyDigits;
    }

    private static Optional<Genero> parseGenero(String genero) {
        if (genero == null || genero.isBlank()) return Optional.empty();
        try {
            return Optional.of(Genero.valueOf(genero.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) return cpf;
        return "***" + cpf.substring(cpf.length() - 4);
    }
}

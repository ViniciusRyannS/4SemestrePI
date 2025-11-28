package br.com.julio.pi.backoffice_users.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.julio.pi.backoffice_users.dto.UsuarioCreateDTO;
import br.com.julio.pi.backoffice_users.dto.UsuarioUpdateDTO;
import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import br.com.julio.pi.backoffice_users.model.enums.Status;
import br.com.julio.pi.backoffice_users.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final SenhaService senhaService;

    public UsuarioService(UsuarioRepository repo, SenhaService senhaService) {
        this.repo = repo;
        this.senhaService = senhaService;
    }

    public Usuario login(String email, String senhaPura) {
        Usuario u = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Credenciais inválidas."));

        if (u.getStatus() == Status.INATIVO) {
            throw new IllegalStateException("Usuário inativo.");
        }
        if (!senhaService.validarSenha(senhaPura, u.getSenha())) {
            throw new IllegalStateException("Credenciais inválidas.");
        }
        return u;
    }

  
    public List<Usuario> findAll() {
        return repo.findAll();
    }

    public Optional<Usuario> findById(long id) {
        return repo.findById(id);
    }

    public Usuario create(UsuarioCreateDTO dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        Usuario u = new Usuario();
        u.setNome(dto.getNome());
        u.setCpf(soDigitos(dto.getCpf())); 
        u.setEmail(dto.getEmail());
        u.setSenha(senhaService.gerarHash(dto.getSenha()));
        u.setGrupo(dto.getGrupo() == null ? Grupo.ESTOQUISTA : dto.getGrupo());
        u.setStatus(Status.ATIVO);
        return repo.save(u);
    }

    public Usuario update(long id, UsuarioUpdateDTO dto) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        u.setNome(dto.getNome());
        u.setCpf(soDigitos(dto.getCpf()));
        u.setGrupo(dto.getGrupo() == null ? u.getGrupo() : dto.getGrupo());
        return repo.save(u);
    }

    public void alterarSenha(long id, String novaSenha) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        u.setSenha(senhaService.gerarHash(novaSenha));
        repo.save(u);
    }

    public Usuario toggleStatus(long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        u.setStatus(u.getStatus() == Status.ATIVO ? Status.INATIVO : Status.ATIVO);
        return repo.save(u);
    }

    private String soDigitos(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }
}

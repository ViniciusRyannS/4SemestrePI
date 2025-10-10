package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.julio.pi.backoffice_users.dto.ProdutoCreateDTO;
import br.com.julio.pi.backoffice_users.dto.ProdutoResponseDTO;
import br.com.julio.pi.backoffice_users.model.ImagemProduto;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import br.com.julio.pi.backoffice_users.repository.ImagemProdutoRepository;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;
import br.com.julio.pi.backoffice_users.util.ImageStorageUtil;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemRepository;
    private final ImageStorageUtil imageStorageUtil;

    public ProdutoService(ProdutoRepository produtoRepository,
                          ImagemProdutoRepository imagemRepository,
                          ImageStorageUtil imageStorageUtil) {
        this.produtoRepository = produtoRepository;
        this.imagemRepository = imagemRepository;
        this.imageStorageUtil = imageStorageUtil;
    }

    // ================= CRUD (ENTIDADE) =================

    @Transactional
    public Produto incluir(Produto p) {
        validarProduto(p);
        Produto salvo = produtoRepository.save(p);
        ajustarPrincipalUnico(salvo);     // no-máx-1 principal
        garantirUmaPrincipal(salvo);      // pelo-menos-1 principal se houver imagens
        return produtoRepository.save(salvo);
    }

    @Transactional
    public Produto alterar(Long id, Produto novosDados) {
        Produto existente = buscarPorId(id);
        aplicarAlteracoes(existente, novosDados);
        validarProduto(existente);
        Produto salvo = produtoRepository.save(existente);
        ajustarPrincipalUnico(salvo);
        garantirUmaPrincipal(salvo);
        return produtoRepository.save(salvo);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: " + id));
    }

    /** Busca com fetch das imagens (evita LazyInitializationException) */
    @Transactional(readOnly = true)
    public Produto buscarPorIdComImagens(Long id) {
        return produtoRepository.findByIdFetchImagens(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto ativar(Long id) {
        Produto p = buscarPorId(id);
        p.setStatus(StatusProduto.ATIVO);
        return produtoRepository.save(p);
    }

    @Transactional
    public Produto inativar(Long id) {
        Produto p = buscarPorId(id);
        p.setStatus(StatusProduto.INATIVO);
        return produtoRepository.save(p);
    }

    // ========= BUSCA e "PAGINAÇÃO" (console) =========

    @Transactional(readOnly = true)
    public List<Produto> listarPorNomeOuTodos(String termo) {
        if (termo != null && !termo.trim().isEmpty()) {
            return produtoRepository.findByNomeContainingIgnoreCaseOrderByIdDesc(termo.trim());
        }
        return produtoRepository.findAllByOrderByIdDesc();
    }

    /** Paginação simples para UI de console: página 1..N, tamanho padrão 10 */
    public List<Produto> pagina(List<Produto> base, int page, int pageSize) {
        if (pageSize <= 0) pageSize = 10;
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(base.size(), from + pageSize);
        if (from >= base.size()) return Collections.emptyList();
        return base.subList(from, to);
    }

    // ================= ESTOQUE (Estoquista) =================

    @Transactional
    public Produto alterarEstoque(Long id, int novoEstoque) {
        if (novoEstoque < 0) throw new IllegalArgumentException("Estoque não pode ser negativo");
        Produto p = buscarPorId(id);
        p.setQtdEstoque(novoEstoque);
        return produtoRepository.save(p);
    }

    // ================= IMAGENS =================

    /** Copia a partir da origem, renomeia e grava apenas o caminho relativo. */
    @Transactional
    public ImagemProduto adicionarImagemFromOrigem(Long produtoId, String caminhoOrigem, boolean principal) {
        if (caminhoOrigem == null || caminhoOrigem.isBlank())
            throw new IllegalArgumentException("Caminho de origem da imagem é obrigatório");

        Produto p = buscarPorIdComImagens(produtoId); // já trago imagens

        try {
            String caminhoRelativo = imageStorageUtil.copyForProduct(produtoId, caminhoOrigem);

            // Se for a primeira imagem do produto, torna-se principal mesmo que não tenha marcado
            boolean primeiraImagem = (p.getImagens() == null || p.getImagens().isEmpty());
            boolean marcarComoPrincipal = principal || primeiraImagem;

            // Se marcou principal, desmarca as demais
            if (marcarComoPrincipal) {
                if (p.getImagens() != null) {
                    for (ImagemProduto i : p.getImagens()) {
                        i.setPrincipal(false);
                    }
                }
            }

            ImagemProduto img = new ImagemProduto(caminhoRelativo, marcarComoPrincipal, p);
            p.getImagens().add(img);

            produtoRepository.save(p);
            ajustarPrincipalUnico(p);
            garantirUmaPrincipal(p);
            return img;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao copiar imagem: " + e.getMessage(), e);
        }
    }

    /** Versão que recebe caminho já pronto. */
    @Transactional
    public ImagemProduto adicionarImagem(Long produtoId, String caminhoDestino, boolean principal) {
        if (caminhoDestino == null || caminhoDestino.isBlank())
            throw new IllegalArgumentException("Caminho da imagem obrigatório");

        Produto p = buscarPorIdComImagens(produtoId);

        boolean primeiraImagem = (p.getImagens() == null || p.getImagens().isEmpty());
        boolean marcarComoPrincipal = principal || primeiraImagem;

        if (marcarComoPrincipal && p.getImagens() != null) {
            for (ImagemProduto i : p.getImagens()) i.setPrincipal(false);
        }

        ImagemProduto img = new ImagemProduto(caminhoDestino, marcarComoPrincipal, p);
        p.getImagens().add(img);
        produtoRepository.save(p);

        ajustarPrincipalUnico(p);
        garantirUmaPrincipal(p);
        return img;
    }

    @Transactional
    public boolean removerImagem(Long produtoId, Long imagemId) {
        Produto p = buscarPorIdComImagens(produtoId);

        List<ImagemProduto> imagens = p.getImagens();
        ImagemProduto alvo = imagens.stream()
                .filter(i -> Objects.equals(i.getId(), imagemId))
                .findFirst()
                .orElse(null);

        if (alvo == null) {
            return false; // não encontrou imagem
        }

        boolean eraPrincipal = alvo.isPrincipal();

        // remove do agregate (orphanRemoval deve cuidar do delete físico no DB)
        imagens.remove(alvo);
        produtoRepository.save(p);

        // Se removi a principal, promovo a mais antiga (ou primeira da lista)
        if (eraPrincipal && !imagens.isEmpty()) {
            // pega a com menor id (mais antiga)
            ImagemProduto proxima = imagens.stream()
                    .min(Comparator.comparing(ImagemProduto::getId))
                    .orElse(imagens.get(0));
            for (ImagemProduto i : imagens) i.setPrincipal(false);
            proxima.setPrincipal(true);
        }

        ajustarPrincipalUnico(p);
        garantirUmaPrincipal(p);

        produtoRepository.save(p);
        return true;
    }

    // ================= REGRAS / VALIDAÇÕES =================

    private void aplicarAlteracoes(Produto existente, Produto novos) {
        if (novos.getNome() != null) existente.setNome(novos.getNome());
        if (novos.getPreco() != null) existente.setPreco(novos.getPreco());
        if (novos.getQtdEstoque() != null) existente.setQtdEstoque(novos.getQtdEstoque());
        if (novos.getDescricaoDetalhada() != null) existente.setDescricaoDetalhada(novos.getDescricaoDetalhada());
        if (novos.getAvaliacao() != null) existente.setAvaliacao(novos.getAvaliacao());
        if (novos.getStatus() != null) existente.setStatus(novos.getStatus());
    }

    private void validarProduto(Produto p) {
        if (p.getNome() == null || p.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (p.getNome().length() > 200)
            throw new IllegalArgumentException("Nome deve ter no máximo 200 caracteres");

        if (p.getPreco() == null) throw new IllegalArgumentException("Preço é obrigatório");
        p.setPreco(p.getPreco().setScale(2, RoundingMode.HALF_UP));
        if (p.getPreco().signum() < 0) throw new IllegalArgumentException("Preço não pode ser negativo");

        if (p.getQtdEstoque() == null || p.getQtdEstoque() < 0)
            throw new IllegalArgumentException("Estoque inválido");

        if (p.getDescricaoDetalhada() != null && p.getDescricaoDetalhada().length() > 2000)
            throw new IllegalArgumentException("Descrição detalhada deve ter no máximo 2000 caracteres");

        if (p.getAvaliacao() != null) {
            BigDecimal a = p.getAvaliacao();
            if (a.compareTo(new BigDecimal("0.5")) < 0 || a.compareTo(new BigDecimal("5.0")) > 0)
                throw new IllegalArgumentException("Avaliação deve estar entre 0.5 e 5.0");
            BigDecimal resto = a.remainder(new BigDecimal("0.5"));
            if (resto.compareTo(BigDecimal.ZERO) != 0)
                throw new IllegalArgumentException("Avaliação deve estar em passos de 0.5");
        }
    }

    /** Garante no máximo 1 imagem principal por produto. */
    private void ajustarPrincipalUnico(Produto p) {
        if (p.getImagens() == null || p.getImagens().isEmpty()) return;
        boolean encontrou = false;
        for (ImagemProduto img : p.getImagens()) {
            if (img.isPrincipal()) {
                if (!encontrou) {
                    encontrou = true;
                } else {
                    img.setPrincipal(false);
                }
            }
        }
    }

    /** Se houver imagens e nenhuma marcada como principal, marca a mais antiga (menor id). */
    private void garantirUmaPrincipal(Produto p) {
        if (p.getImagens() == null || p.getImagens().isEmpty()) return;

        boolean haPrincipal = p.getImagens().stream().anyMatch(ImagemProduto::isPrincipal);
        if (!haPrincipal) {
            ImagemProduto antiga = p.getImagens().stream()
                    .min(Comparator.comparing(ImagemProduto::getId))
                    .orElse(p.getImagens().get(0));
            antiga.setPrincipal(true);
        }
    }

    // ======================== Camada DTO ========================

    /** Lista já em formato DTO (mais novos primeiro). */
    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarDTO() {
        return produtoRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Busca por id já em formato DTO, com imagens carregadas. */
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarDTO(Long id) {
        Produto p = buscarPorIdComImagens(id);
        return toResponse(p);
    }

    /** Inclui a partir do DTO de criação e retorna o DTO de resposta. */
    @Transactional
    public ProdutoResponseDTO incluirDTO(ProdutoCreateDTO dto) {
        Produto novo = fromCreateDTO(dto);
        Produto salvo = incluir(novo); // reaproveita validações e regras existentes
        return toResponse(salvo);
    }

    // ---------- mapeadores ----------
    private Produto fromCreateDTO(ProdutoCreateDTO dto) {
        Produto p = new Produto();
        p.setNome(dto.getNome());
        p.setPreco(dto.getPreco());
        p.setQtdEstoque(dto.getQtdEstoque());
        p.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        p.setAvaliacao(dto.getAvaliacao());
        p.setStatus(dto.getStatus() == null ? StatusProduto.ATIVO : dto.getStatus());
        return p;
    }

    private ProdutoResponseDTO toResponse(Produto p) {
        boolean estoqueBaixo = p.getQtdEstoque() != null && p.getQtdEstoque() <= 3;
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getPreco(),
                p.getQtdEstoque(),
                p.getDescricaoDetalhada(),
                p.getAvaliacao(),
                p.getStatus(),
                estoqueBaixo
        );
    }
}

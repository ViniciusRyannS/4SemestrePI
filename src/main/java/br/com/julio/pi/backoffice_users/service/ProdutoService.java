package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.julio.pi.backoffice_users.model.ImagemProduto;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import br.com.julio.pi.backoffice_users.repository.ImagemProdutoRepository;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemRepository;

    public ProdutoService(ProdutoRepository produtoRepository, ImagemProdutoRepository imagemRepository) {
        this.produtoRepository = produtoRepository;
        this.imagemRepository = imagemRepository;
    }

    // ================= CRUD =================

    @Transactional
    public Produto incluir(Produto p) {
        validarProduto(p);
        Produto salvo = produtoRepository.save(p);
        ajustarPrincipalUnico(salvo);
        return salvo;
    }

    @Transactional
    public Produto alterar(Long id, Produto novosDados) {
        Produto existente = buscarPorId(id);
        aplicarAlteracoes(existente, novosDados);
        validarProduto(existente);
        Produto salvo = produtoRepository.save(existente);
        ajustarPrincipalUnico(salvo);
        return salvo;
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
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

    @Transactional
    public ImagemProduto adicionarImagem(Long produtoId, String caminhoDestino, boolean principal) {
        if (caminhoDestino == null || caminhoDestino.isBlank())
            throw new IllegalArgumentException("Caminho da imagem obrigatório");
        Produto p = buscarPorId(produtoId);
        ImagemProduto img = new ImagemProduto(caminhoDestino, principal, p);
        p.getImagens().add(img);
        produtoRepository.save(p);
        ajustarPrincipalUnico(p);
        return img;
    }

    @Transactional
    public void removerImagem(Long produtoId, Long imagemId) {
        Produto p = buscarPorId(produtoId);
        p.setImagens(p.getImagens().stream()
                .filter(i -> !Objects.equals(i.getId(), imagemId))
                .collect(Collectors.toList()));
        produtoRepository.save(p);
        ajustarPrincipalUnico(p);
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
            // passos de 0.5:
            BigDecimal resto = a.remainder(new BigDecimal("0.5"));
            if (resto.compareTo(BigDecimal.ZERO) != 0)
                throw new IllegalArgumentException("Avaliação deve estar em passos de 0.5");
        }
    }

    /** Garante no máximo 1 imagem principal por produto */
    private void ajustarPrincipalUnico(Produto p) {
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
}

package br.com.julio.pi.backoffice_users.dto;

import java.math.BigDecimal;

import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;

public class ProdutoResponseDTO {

    private Long id;
    private String nome;
    private BigDecimal preco;
    private Integer qtdEstoque;
    private String descricaoDetalhada;
    private BigDecimal avaliacao;
    private StatusProduto status;
    private boolean estoqueBaixo;

    // caminho/URL da imagem principal (pode ser null)
    private String imagemPrincipal;

    public ProdutoResponseDTO() {}

    // compatibilidade sem imagemPrincipal
    public ProdutoResponseDTO(Long id, String nome, BigDecimal preco, Integer qtdEstoque,
                              String descricaoDetalhada, BigDecimal avaliacao,
                              StatusProduto status, boolean estoqueBaixo) {
        this(id, nome, preco, qtdEstoque, descricaoDetalhada, avaliacao, status, estoqueBaixo, null);
    }

    // construtor com imagemPrincipal
    private ProdutoResponseDTO(Long id, String nome, BigDecimal preco, Integer qtdEstoque,
                               String descricaoDetalhada, BigDecimal avaliacao,
                               StatusProduto status, boolean estoqueBaixo,
                               String imagemPrincipal) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.qtdEstoque = qtdEstoque;
        this.descricaoDetalhada = descricaoDetalhada;
        this.avaliacao = avaliacao;
        this.status = status;
        this.estoqueBaixo = estoqueBaixo;
        this.imagemPrincipal = imagemPrincipal;
    }

    /** DTO sem imagem principal (compat). */
    public static ProdutoResponseDTO of(Produto p) {
        boolean low = p.getQtdEstoque() != null && p.getQtdEstoque() <= 3;
        return new ProdutoResponseDTO(
            p.getId(), p.getNome(), p.getPreco(), p.getQtdEstoque(),
            p.getDescricaoDetalhada(), p.getAvaliacao(), p.getStatus(), low,
            null
        );
    }

    /** DTO com imagem principal (normaliza caminho para URL web). */
    public static ProdutoResponseDTO of(Produto p, String imagemPrincipal) {
        boolean low = p.getQtdEstoque() != null && p.getQtdEstoque() <= 3;
        return new ProdutoResponseDTO(
            p.getId(), p.getNome(), p.getPreco(), p.getQtdEstoque(),
            p.getDescricaoDetalhada(), p.getAvaliacao(), p.getStatus(), low,
            normalizeWebPath(imagemPrincipal)
        );
    }

    // --- normalização do caminho para algo como "/imagens/5/arquivo.jpg"
    private static String normalizeWebPath(String s) {
        if (s == null || s.isBlank()) return null;
        s = s.replace('\\', '/');
        if (!s.startsWith("/")) s = "/" + s;
        return s;
    }

    // getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public Integer getQtdEstoque() { return qtdEstoque; }
    public String getDescricaoDetalhada() { return descricaoDetalhada; }
    public BigDecimal getAvaliacao() { return avaliacao; }
    public StatusProduto getStatus() { return status; }
    public boolean isEstoqueBaixo() { return estoqueBaixo; }
    public String getImagemPrincipal() { return imagemPrincipal; }
}

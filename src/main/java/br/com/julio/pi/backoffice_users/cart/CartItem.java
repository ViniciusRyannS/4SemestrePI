package br.com.julio.pi.backoffice_users.cart;

import java.math.BigDecimal;

public class CartItem {
    private Long produtoId;
    private String nome;
    private BigDecimal preco; 
    private int quantidade;

    public CartItem() {}

    public CartItem(Long produtoId, String nome, BigDecimal preco, int quantidade) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getTotalLinha() {
        if (preco == null) return BigDecimal.ZERO;
        return preco.multiply(BigDecimal.valueOf(Math.max(quantidade, 0)));
    }
}

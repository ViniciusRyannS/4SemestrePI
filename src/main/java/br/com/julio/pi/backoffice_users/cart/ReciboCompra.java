package br.com.julio.pi.backoffice_users.cart;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ReciboCompra {
    public static class Item {
        private Long produtoId;
        private String nome;
        private BigDecimal preco;
        private int quantidade;
        private BigDecimal totalLinha;

        public Item() {}
        public Item(Long produtoId, String nome, BigDecimal preco, int quantidade, BigDecimal totalLinha) {
            this.produtoId = produtoId; this.nome = nome; this.preco = preco; this.quantidade = quantidade; this.totalLinha = totalLinha;
        }
        public Long getProdutoId() { return produtoId; }
        public String getNome() { return nome; }
        public BigDecimal getPreco() { return preco; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getTotalLinha() { return totalLinha; }
    }

    private String pedidoId;
    private OffsetDateTime emitidoEm;
    private List<Item> itens;
    private String modalidadeFrete;
    private BigDecimal subtotal;
    private BigDecimal frete;
    private BigDecimal total;

    public ReciboCompra() {}
    public ReciboCompra(String pedidoId, OffsetDateTime emitidoEm, List<Item> itens, String modalidadeFrete,
                        BigDecimal subtotal, BigDecimal frete, BigDecimal total) {
        this.pedidoId = pedidoId; this.emitidoEm = emitidoEm; this.itens = itens; this.modalidadeFrete = modalidadeFrete;
        this.subtotal = subtotal; this.frete = frete; this.total = total;
    }

    public String getPedidoId() { return pedidoId; }
    public OffsetDateTime getEmitidoEm() { return emitidoEm; }
    public List<Item> getItens() { return itens; }
    public String getModalidadeFrete() { return modalidadeFrete; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getFrete() { return frete; }
    public BigDecimal getTotal() { return total; }
}

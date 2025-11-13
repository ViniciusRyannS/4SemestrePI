package br.com.julio.pi.backoffice_users.cart;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** aq devolcendo recibo da compra */
public class ReciboCompra {

    public static class Item {
        private Long produtoId;
        private String nome;
        private BigDecimal preco;
        private int quantidade;
        private BigDecimal totalLinha;

        public Item() {}
        public Item(Long produtoId, String nome, BigDecimal preco, int quantidade, BigDecimal totalLinha) {
            this.produtoId = produtoId;
            this.nome = nome;
            this.preco = preco;
            this.quantidade = quantidade;
            this.totalLinha = totalLinha;
        }
        public Long getProdutoId() { return produtoId; }
        public String getNome() { return nome; }
        public BigDecimal getPreco() { return preco; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getTotalLinha() { return totalLinha; }
    }

    private String pedidoId;
    private String codigo;              
    private OffsetDateTime emitidoEm;
    private List<Item> itens;

    private String modalidadeFrete;
    private BigDecimal subtotal;
    private BigDecimal frete;
    private BigDecimal total;

    private Long enderecoEntregaId;
    private String formaPagamento;

    public ReciboCompra() {}

    public ReciboCompra(
            String pedidoId,
            OffsetDateTime emitidoEm,
            List<Item> itens,
            String modalidadeFrete,
            BigDecimal subtotal,
            BigDecimal frete,
            BigDecimal total
    ) {
        this.pedidoId = pedidoId;
        this.emitidoEm = emitidoEm;
        this.itens = itens;
        this.modalidadeFrete = modalidadeFrete;
        this.subtotal = subtotal;
        this.frete = frete;
        this.total = total;
    }

    public ReciboCompra(
            String pedidoId,
            OffsetDateTime emitidoEm,
            List<Item> itens,
            String modalidadeFrete,
            BigDecimal subtotal,
            BigDecimal frete,
            BigDecimal total,
            Long enderecoEntregaId,
            String formaPagamento
    ) {
        this(pedidoId, emitidoEm, itens, modalidadeFrete, subtotal, frete, total);
        this.enderecoEntregaId = enderecoEntregaId;
        this.formaPagamento = formaPagamento;
    }

    public ReciboCompra(
            String pedidoId,
            String codigo,
            OffsetDateTime emitidoEm,
            List<Item> itens,
            String modalidadeFrete,
            BigDecimal subtotal,
            BigDecimal frete,
            BigDecimal total,
            Long enderecoEntregaId,
            String formaPagamento
    ) {
        this(pedidoId, emitidoEm, itens, modalidadeFrete, subtotal, frete, total, enderecoEntregaId, formaPagamento);
        this.codigo = codigo;
    }

    public String getPedidoId() { return pedidoId; }
    public String getCodigo()   { return codigo; }         
    public OffsetDateTime getEmitidoEm() { return emitidoEm; }
    public List<Item> getItens() { return itens; }
    public String getModalidadeFrete() { return modalidadeFrete; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getFrete() { return frete; }
    public BigDecimal getTotal() { return total; }

    public Long getEnderecoEntregaId() { return enderecoEntregaId; }
    public String getFormaPagamento() { return formaPagamento; }

    public void setEnderecoEntregaId(Long enderecoEntregaId) { this.enderecoEntregaId = enderecoEntregaId; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}

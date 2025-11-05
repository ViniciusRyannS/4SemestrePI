package br.com.julio.pi.backoffice_users.model.pedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.julio.pi.backoffice_users.model.enums.StatusPedido;
import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // snapshot de quem comprou (simples: apenas id do cliente)
    private Long clienteId;

    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.AGUARDANDO_PAGAMENTO;

    // frete/total
    @Column(precision=12, scale=2) private BigDecimal subtotal;
    @Column(precision=12, scale=2) private BigDecimal frete;
    @Column(precision=12, scale=2) private BigDecimal total;
    private String modalidadeFrete;

    // pagamento
    private String formaPagamento; // "BOLETO" | "CARTAO"
    private Integer parcelas;      // se cartão

    // snapshot de endereço escolhido (texto simples)
    private String endLogradouro;
    private String endNumero;
    private String endComplemento;
    private String endBairro;
    private String endCidade;
    private String endUf;
    private String endCep;

    @OneToMany(mappedBy="pedido", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    // getters e helpers
    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) { this.frete = frete; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getModalidadeFrete() { return modalidadeFrete; }
    public void setModalidadeFrete(String modalidadeFrete) { this.modalidadeFrete = modalidadeFrete; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }

    public String getEndLogradouro() { return endLogradouro; }
    public void setEndLogradouro(String v) { this.endLogradouro = v; }
    public String getEndNumero() { return endNumero; }
    public void setEndNumero(String v) { this.endNumero = v; }
    public String getEndComplemento() { return endComplemento; }
    public void setEndComplemento(String v) { this.endComplemento = v; }
    public String getEndBairro() { return endBairro; }
    public void setEndBairro(String v) { this.endBairro = v; }
    public String getEndCidade() { return endCidade; }
    public void setEndCidade(String v) { this.endCidade = v; }
    public String getEndUf() { return endUf; }
    public void setEndUf(String v) { this.endUf = v; }
    public String getEndCep() { return endCep; }
    public void setEndCep(String v) { this.endCep = v; }

    public List<PedidoItem> getItens() { return itens; }
}

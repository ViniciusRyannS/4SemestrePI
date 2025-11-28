package br.com.julio.pi.backoffice_users.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Carrinho {

    private final Map<Long, CartItem> itens = new LinkedHashMap<>();
   
    private BigDecimal frete;                
    private String modalidadeFrete;          
    
    private Long enderecoEntregaId;           
    private String formaPagamento;           

    public void adicionarOuSomar(Long produtoId, String nome, BigDecimal preco, int qtd) {
        if (qtd <= 0) return;
        CartItem item = itens.get(produtoId);
        if (item == null) {
            itens.put(produtoId, new CartItem(produtoId, nome, preco, qtd));
        } else {
            item.setQuantidade(item.getQuantidade() + qtd);
            item.setPreco(preco);
        }
    }

    public void atualizarQuantidade(Long produtoId, int novaQtd) {
        if (!itens.containsKey(produtoId)) return;
        if (novaQtd <= 0) {
            itens.remove(produtoId);
        } else {
            itens.get(produtoId).setQuantidade(novaQtd);
        }
    }

    public void remover(Long produtoId) {
        itens.remove(produtoId);
    }

    public List<CartItem> getItens() {
        return new ArrayList<>(itens.values());
    }

    public BigDecimal getSubtotal() {
        return itens.values().stream()
                .map(CartItem::getTotalLinha)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFrete() {
        return frete == null ? BigDecimal.ZERO : frete;
    }

    public String getModalidadeFrete() {
        return modalidadeFrete;
    }

    public void aplicarFrete(String modalidade, BigDecimal valor) {
        this.modalidadeFrete = modalidade;
        this.frete = (valor == null ? BigDecimal.ZERO : valor);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getFrete());
    }

    public Long getEnderecoEntregaId() { return enderecoEntregaId; }
    public void setEnderecoEntregaId(Long enderecoEntregaId) { this.enderecoEntregaId = enderecoEntregaId; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public void limparSelecoesCheckout() {
        this.enderecoEntregaId = null;
        this.formaPagamento = null;
    }

    
    public Map<Long, CartItem> getItensMap() {
        return itens;
    }
}

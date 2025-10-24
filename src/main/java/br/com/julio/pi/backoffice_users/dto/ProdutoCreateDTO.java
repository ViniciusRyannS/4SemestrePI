package br.com.julio.pi.backoffice_users.dto;

import java.math.BigDecimal;

import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProdutoCreateDTO {

    @NotBlank @Size(max = 200)
    private String nome;

    @NotNull @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;

    @NotNull @Min(0)
    private Integer qtdEstoque;

    @NotBlank @Size(min = 10, max = 2000)
    private String descricaoDetalhada;

    // opcional: 0.5 a 5.0 passos de 0.5 (validado no service)
    private BigDecimal avaliacao;

    // NOVO: opcional. Se vier null, assume ATIVO
    private StatusProduto status;

    // getters/setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getQtdEstoque() { return qtdEstoque; }
    public void setQtdEstoque(Integer qtdEstoque) { this.qtdEstoque = qtdEstoque; }
    public String getDescricaoDetalhada() { return descricaoDetalhada; }
    public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }
    public BigDecimal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(BigDecimal avaliacao) { this.avaliacao = avaliacao; }
    public StatusProduto getStatus() { return status; }
    public void setStatus(StatusProduto status) { this.status = status; }

    public Produto toEntity() {
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(preco);
        p.setQtdEstoque(qtdEstoque);
        p.setDescricaoDetalhada(descricaoDetalhada);
        p.setAvaliacao(avaliacao);
        p.setStatus(status != null ? status : StatusProduto.ATIVO);
        return p;
    }
}


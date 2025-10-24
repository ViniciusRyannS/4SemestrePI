package br.com.julio.pi.backoffice_users.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String nome;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer qtdEstoque;

    @Column(length = 2000)
    private String descricaoDetalhada;

    // Avaliação 0.5 a 5.0 (validado no service)
    @Column(precision = 2, scale = 1)
    private BigDecimal avaliacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusProduto status = StatusProduto.ATIVO;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagemProduto> imagens = new ArrayList<>();

    public Produto() {}

    public Produto(String nome, BigDecimal preco, Integer qtdEstoque, String descricaoDetalhada,
                   BigDecimal avaliacao, StatusProduto status) {
        this.nome = nome;
        this.preco = preco;
        this.qtdEstoque = qtdEstoque;
        this.descricaoDetalhada = descricaoDetalhada;
        this.avaliacao = avaliacao;
        this.status = status == null ? StatusProduto.ATIVO : status;
    }

    // ---------- helpers ----------
    /** Estoque baixo quando <= 3 (regra usada na tela). */
    public boolean isEstoqueBaixo() {
        return qtdEstoque != null && qtdEstoque <= 3;
    }

    /** Mantém a consistência do relacionamento bidirecional. */
    public void addImagem(ImagemProduto img) {
        if (img == null) return;
        imagens.add(img);
        img.setProduto(this);
    }

    public void removeImagem(ImagemProduto img) {
        if (img == null) return;
        imagens.remove(img);
        img.setProduto(null);
    }

    // ---------- getters/setters ----------
    public Long getId() { return id; }

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
    public void setStatus(StatusProduto status) { this.status = (status == null ? StatusProduto.ATIVO : status); }

    public List<ImagemProduto> getImagens() { return imagens; }
    public void setImagens(List<ImagemProduto> imagens) { this.imagens = imagens == null ? new ArrayList<>() : imagens; }

    // ---------- equals/hashCode por id ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produto)) return false;
        Produto other = (Produto) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

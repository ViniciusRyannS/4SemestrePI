package br.com.julio.pi.backoffice_users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "imagens_produto")
public class ImagemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // caminho final (destino) onde a imagem ficará vinculada ao produto
    @Column(nullable = false, length = 512)
    private String caminhoDestino;

    @Column(nullable = false)
    private boolean principal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    public ImagemProduto() {}

    public ImagemProduto(String caminhoDestino, boolean principal, Produto produto) {

          this.caminhoDestino = caminhoDestino;
    this.principal = principal;
    this.produto = produto;
        this.caminhoDestino = caminhoDestino;
        this.principal = principal;
        this.produto = produto;
    }

    public Long getId() { return id; }
    public String getCaminhoDestino() { return caminhoDestino; }
    public void setCaminhoDestino(String caminhoDestino) { this.caminhoDestino = caminhoDestino; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
}

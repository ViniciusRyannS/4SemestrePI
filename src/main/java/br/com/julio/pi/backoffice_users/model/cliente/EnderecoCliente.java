package br.com.julio.pi.backoffice_users.model.cliente;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="enderecos_cliente")
public class EnderecoCliente {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JsonBackReference
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private TipoEndereco tipo;

    private boolean padrao;

    @Column(nullable=false, length=8)
    private String cep;

    @Column(nullable=true, length=120) private String logradouro;
    @Column(nullable=true, length=60)  private String bairro;
    @Column(nullable=true, length=60)  private String cidade;
    @Column(nullable=true, length=2)   private String uf;

    @Column(nullable=false, length=15) private String numero;
    @Column(nullable=true, length=60)  private String complemento;

    private Instant criadoEm = Instant.now();

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (cep != null) {
            cep = cep.replaceAll("\\D", "");
        }
        if (uf != null) uf = uf.trim().toUpperCase();
        if (logradouro != null)  logradouro  = trimOrNull(logradouro);
        if (bairro != null)      bairro      = trimOrNull(bairro);
        if (cidade != null)      cidade      = trimOrNull(cidade);
        if (numero != null)      numero      = numero.trim();
        if (complemento != null) complemento = trimOrNull(complemento);
    }

    private String trimOrNull(String s) {
        String t = (s == null ? null : s.trim());
        return (t == null || t.isEmpty()) ? null : t;
    }

    public Long getId(){ return id; }

    public Cliente getCliente(){ return cliente; }
    public void setCliente(Cliente v){ this.cliente = v; }

    public TipoEndereco getTipo(){ return tipo; }
    public void setTipo(TipoEndereco v){ this.tipo = v; }

    public boolean isPadrao(){ return padrao; }
    public void setPadrao(boolean v){ this.padrao = v; }

    public String getCep(){ return cep; }
    public void setCep(String v){ this.cep = v; }

    public String getLogradouro(){ return logradouro; }
    public void setLogradouro(String v){ this.logradouro = v; }

    public String getBairro(){ return bairro; }
    public void setBairro(String v){ this.bairro = v; }

    public String getCidade(){ return cidade; }
    public void setCidade(String v){ this.cidade = v; }

    public String getUf(){ return uf; }
    public void setUf(String v){ this.uf = v; }

    public String getNumero(){ return numero; }
    public void setNumero(String v){ this.numero = v; }

    public String getComplemento(){ return complemento; }
    public void setComplemento(String v){ this.complemento = v; }

    public Instant getCriadoEm(){ return criadoEm; }
    public void setCriadoEm(Instant criadoEm){ this.criadoEm = criadoEm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnderecoCliente)) return false;
        EnderecoCliente other = (EnderecoCliente) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package br.com.julio.pi.backoffice_users.model.cliente;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "clientes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf")
    }
)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String nomeCompleto;

    @Column(nullable=false, length=150)
    private String email;

    @Column(nullable=false, length=14)
    private String cpf;

    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(length=15)
    private Genero genero;

    @Column(nullable=false, length=200)
    @JsonIgnore              // <-- não expõe a senha no JSON
    private String senha;    // hash

    private Instant criadoEm = Instant.now();

    @OneToMany(mappedBy="cliente", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    @JsonManagedReference    // <-- evita recursão (lado "pai")
    private List<EnderecoCliente> enderecos = new ArrayList<>();

    // getters/setters
    public Long getId(){ return id; }

    public String getNomeCompleto(){ return nomeCompleto; }
    public void setNomeCompleto(String v){ this.nomeCompleto = v; }

    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email = v; }

    public String getCpf(){ return cpf; }
    public void setCpf(String v){ this.cpf = v; }

    public LocalDate getDataNascimento(){ return dataNascimento; }
    public void setDataNascimento(LocalDate v){ this.dataNascimento = v; }

    public Genero getGenero(){ return genero; }
    public void setGenero(Genero v){ this.genero = v; }

    public String getSenha(){ return senha; }
    public void setSenha(String v){ this.senha = v; }

    public List<EnderecoCliente> getEnderecos(){ return enderecos; }

    public Instant getCriadoEm(){ return criadoEm; }
    public void setCriadoEm(Instant criadoEm){ this.criadoEm = criadoEm; }
}

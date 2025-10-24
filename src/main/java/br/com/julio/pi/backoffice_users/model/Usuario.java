package br.com.julio.pi.backoffice_users.model;

import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import br.com.julio.pi.backoffice_users.model.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Column(length = 14)
    private String cpf;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 6)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    /* ===================== ADIÇÕES PARA AUTENTICAÇÃO ===================== */

    /**
     * Exposto para o AuthController/JWT como "perfil".
     * Retorna algo como "ADMIN" ou "ESTOQUISTA" baseado no enum Grupo.
     */
    public String getPerfil() {
        return (grupo != null) ? grupo.name() : null;
    }

    /** Atalho para verificar se o usuário está ativo. */
    public boolean isAtivo() {
        return status != null && status.name().equalsIgnoreCase("ATIVO");
    }

    /** Conveniência para checar papel/perfil em regras pontuais. */
    public boolean hasPerfil(String perfil) {
        if (perfil == null || grupo == null) return false;
        return grupo.name().equalsIgnoreCase(perfil);
    }
}

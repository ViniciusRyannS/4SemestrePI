package br.com.julio.pi.backoffice_users.dto;

import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioCreateDTO {
    @NotBlank private String nome;
    @NotBlank private String cpf;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String senha;
    @NotBlank @Size(min = 6) private String confirmarSenha;
    private Grupo grupo; 

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
}

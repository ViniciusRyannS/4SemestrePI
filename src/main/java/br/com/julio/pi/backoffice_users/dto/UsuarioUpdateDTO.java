package br.com.julio.pi.backoffice_users.dto;

import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import jakarta.validation.constraints.NotBlank;

public class UsuarioUpdateDTO {
    @NotBlank private String nome;
    @NotBlank private String cpf;
    private Grupo grupo;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
}

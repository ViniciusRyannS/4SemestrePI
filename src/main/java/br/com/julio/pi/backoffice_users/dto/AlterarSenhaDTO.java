package br.com.julio.pi.backoffice_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlterarSenhaDTO {
    @NotBlank @Size(min = 6) private String novaSenha;
    @NotBlank @Size(min = 6) private String confirmarSenha;

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
}

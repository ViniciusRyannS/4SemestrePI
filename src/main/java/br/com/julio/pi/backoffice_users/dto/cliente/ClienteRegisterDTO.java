package br.com.julio.pi.backoffice_users.dto.cliente;

import java.time.LocalDate;

public record ClienteRegisterDTO(
    String nomeCompleto,
    String email,
    String cpf,
    LocalDate dataNascimento,
    String genero,          // "MASCULINO"/"FEMININO"/"OUTRO"
    String senha,
    String confirmaSenha,

    // Endereço de faturamento (obrigatório)
    String fatCep, String fatNumero, String fatComplemento,

    // Endereço de entrega: se null, copiar faturamento
    String entCep, String entNumero, String entComplemento
) {}

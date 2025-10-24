package br.com.julio.pi.backoffice_users.dto.cliente;

import java.time.Instant;

public record ClienteLoginResponse(
    String token, Instant expiresAt, String nome, String email, String tipo
) {}

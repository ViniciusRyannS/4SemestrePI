package br.com.julio.pi.backoffice_users.dto.cliente;

import java.time.LocalDate;
public record ClienteUpdateDTO(String nomeCompleto, LocalDate dataNascimento, String genero) {}

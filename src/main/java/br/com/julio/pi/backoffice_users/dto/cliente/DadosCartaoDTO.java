package br.com.julio.pi.backoffice_users.dto.cliente;


public record DadosCartaoDTO(
  String numero,
  String nome,
  String validade, // MM/AA
  String cvv,
  Integer parcelas
) {}

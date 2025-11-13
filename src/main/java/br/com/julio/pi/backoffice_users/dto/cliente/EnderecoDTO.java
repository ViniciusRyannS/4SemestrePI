package br.com.julio.pi.backoffice_users.dto.cliente;

import br.com.julio.pi.backoffice_users.model.cliente.TipoEndereco;

public record EnderecoDTO(
    Long id,
    TipoEndereco tipo,
    boolean padrao,
    String cep, String logradouro, String bairro, String cidade, String uf,
    String numero, String complemento
) {}

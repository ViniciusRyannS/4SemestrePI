package br.com.julio.pi.backoffice_users.dto.cliente;

import br.com.julio.pi.backoffice_users.model.cliente.TipoEndereco;

public record EnderecoCreateDTO(
    TipoEndereco tipo,
    String cep, String numero, String complemento,
    boolean padrao // só terá efeito se tipo==ENTREGA
) {}

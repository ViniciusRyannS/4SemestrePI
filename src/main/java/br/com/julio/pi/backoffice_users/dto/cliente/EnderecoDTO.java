package br.com.julio.pi.backoffice_users.dto.cliente;

import br.com.julio.pi.backoffice_users.model.cliente.EnderecoCliente;
import br.com.julio.pi.backoffice_users.model.cliente.TipoEndereco;

public record EnderecoDTO(
        Long id,
        TipoEndereco tipo,
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf,
        String numero,
        String complemento,
        boolean padrao
) {
    public static EnderecoDTO from(EnderecoCliente e) {
        return new EnderecoDTO(
                e.getId(),
                e.getTipo(),
                e.getCep(),
                e.getLogradouro(),
                e.getBairro(),
                e.getCidade(),
                e.getUf(),
                e.getNumero(),
                e.getComplemento(),
                e.isPadrao()
        );
    }
}

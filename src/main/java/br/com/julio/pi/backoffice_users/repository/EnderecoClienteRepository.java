package br.com.julio.pi.backoffice_users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.cliente.EnderecoCliente;
import br.com.julio.pi.backoffice_users.model.cliente.TipoEndereco;

public interface EnderecoClienteRepository extends JpaRepository<EnderecoCliente, Long> {
    List<EnderecoCliente> findByClienteIdAndTipoOrderByIdDesc(Long clienteId, TipoEndereco tipo);
    List<EnderecoCliente> findByClienteIdOrderByIdDesc(Long clienteId);
}

package br.com.julio.pi.backoffice_users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.pedido.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);

    Optional<Pedido> findByCodigoAndClienteId(String codigo, Long clienteId); // <— NOVO

    
}

package br.com.julio.pi.backoffice_users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.cliente.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}

package br.com.julio.pi.backoffice_users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}

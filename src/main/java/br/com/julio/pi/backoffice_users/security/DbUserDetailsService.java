package br.com.julio.pi.backoffice_users.security;

import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public DbUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // >>>>>> IMPORTANTE: se o seu getter NÃO for getPerfil(), troque AQUI (ex.: getPapel(), getTipo(), etc.)
        String role = u.getPerfil(); // <---- ajuste o nome do getter se precisar
        if (role == null || role.isBlank()) role = "USER";
        if (!role.startsWith("ROLE_")) role = "ROLE_" + role.toUpperCase();

        return User.builder()
                .username(u.getEmail())
                .password(u.getSenha())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}

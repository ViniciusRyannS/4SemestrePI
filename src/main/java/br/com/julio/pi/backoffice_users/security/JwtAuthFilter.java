package br.com.julio.pi.backoffice_users.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.model.cliente.Cliente;
import br.com.julio.pi.backoffice_users.repository.ClienteRepository;
import br.com.julio.pi.backoffice_users.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;

    public JwtAuthFilter(JwtService jwt, UsuarioRepository usuarios, ClienteRepository clientes) {
        this.jwt = jwt; this.usuarios = usuarios; this.clientes = clientes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res); // sem token -> segue
            return;
        }

        String token = header.substring(7);
        try {
            Jws<Claims> jws = jwt.parse(token);
            Claims body = jws.getBody();
            String email = body.getSubject();
            String tipo  = (String) body.get("tipo"); // "ADM" ou "CLIENTE"
            if (tipo == null) tipo = "ADM"; // compat

            if ("CLIENTE".equalsIgnoreCase(tipo)) {
                Cliente c = clientes.findByEmail(email).orElse(null);
                if (c != null) {
                    // anexa o id para os controllers
                    req.setAttribute("CLIENT_ID", c.getId());
                    var auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                // fluxo admin/estoquista
                Usuario u = usuarios.findByEmail(email).orElse(null);
                if (u != null) {
                    String role = "ROLE_" + (u.getGrupo() == null ? "ESTOQUISTA" : u.getGrupo().name());
                    var auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority(role)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext(); // token inválido -> sem auth
        }
        chain.doFilter(req, res);
    }
}

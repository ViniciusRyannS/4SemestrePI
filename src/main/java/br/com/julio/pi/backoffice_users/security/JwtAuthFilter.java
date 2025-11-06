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
import br.com.julio.pi.backoffice_users.security.cliente.AuthClientePrincipal;
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
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.clientes = clientes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
                System.out.println("URL: " + req.getRequestURL() + " | Auth: " + req.getHeader("Authorization"));
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

    // 1) Se o tipo disser CLIENTE, usa CLIENTE
    if ("CLIENTE".equalsIgnoreCase(tipo)) {
        clientes.findByEmail(email).ifPresent(c -> {
            req.setAttribute("CLIENT_ID", c.getId());
            var auth = new UsernamePasswordAuthenticationToken(
                new AuthClientePrincipal(c.getId(), c.getEmail()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
    }
    // 2) Se o tipo disser ADM, usa backoffice
    else if ("ADM".equalsIgnoreCase(tipo)) {
        usuarios.findByEmail(email).ifPresent(u -> {
            String role = "ROLE_" + (u.getGrupo() == null ? "ESTOQUISTA" : u.getGrupo().name());
            var auth = new UsernamePasswordAuthenticationToken(u.getEmail(), null, List.of(new SimpleGrantedAuthority(role)));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
    }
    // 3) Tipo ausente: tenta cliente primeiro; se não achar, tenta backoffice
    else {
        if (clientes.findByEmail(email).isPresent()) {
            var c = clientes.findByEmail(email).get();
            req.setAttribute("CLIENT_ID", c.getId());
            var auth = new UsernamePasswordAuthenticationToken(
                new AuthClientePrincipal(c.getId(), c.getEmail()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            usuarios.findByEmail(email).ifPresent(u -> {
                String role = "ROLE_" + (u.getGrupo() == null ? "ESTOQUISTA" : u.getGrupo().name());
                var auth = new UsernamePasswordAuthenticationToken(u.getEmail(), null, List.of(new SimpleGrantedAuthority(role)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
    }
} catch (Exception ex) {
    SecurityContextHolder.clearContext();
}

        chain.doFilter(req, res);
    }
}

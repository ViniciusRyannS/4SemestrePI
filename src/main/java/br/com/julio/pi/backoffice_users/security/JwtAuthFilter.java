package br.com.julio.pi.backoffice_users.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // públicos
        if (path.startsWith("/h2-console")) return true;
        if (path.equals("/") || path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) return true;
        if (path.startsWith("/imagens/")) return true;
        if (path.startsWith("/api/auth/login")) return true;
        if (path.startsWith("/api/carrinho") || path.startsWith("/api/frete")) return true;

        // GET de produtos é público; POST/DELETE exigem auth
        if (path.startsWith("/api/produtos") && HttpMethod.GET.matches(request.getMethod())) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Jws<Claims> jws = jwtService.parse(token);
            Claims claims = jws.getBody();

            String username = claims.getSubject(); // email
            String grupo = (String) claims.get("grupo"); // ADMIN, ESTOQUISTA, etc.

            // monta autoridade Spring Security
            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    grupo != null ? List.of(new SimpleGrantedAuthority("ROLE_" + grupo)) : List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // token inválido -> não autentica
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

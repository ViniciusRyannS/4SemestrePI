package br.com.julio.pi.backoffice_users.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.model.enums.Status;
import br.com.julio.pi.backoffice_users.repository.UsuarioRepository;
import br.com.julio.pi.backoffice_users.service.SenhaService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final SenhaService senhaService;

    public AuthController(UsuarioRepository usuarioRepository,
                          JwtService jwtService,
                          SenhaService senhaService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.senhaService = senhaService;
    }

    // ==== DTOs ====
    public static record LoginRequest(String email, String senha) {}
    public static record LoginResponse(String token, Instant expiresAt, String nome, String email, String grupo) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.email() == null || req.senha() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Credenciais inválidas");
        }

        final String email = req.email().trim();
        final String senhaPura = req.senha();

        Usuario u = usuarioRepository.findByEmail(email).orElse(null);
        if (u == null) {
            log.warn("Login falhou: email não encontrado {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário/senha inválidos");
        }

        if (u.getStatus() == null || u.getStatus() == Status.INATIVO) {
            log.warn("Login bloqueado: usuário {} INATIVO", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário inativo");
        }

        // >>> AQUI: compara usando BCrypt <<<
        boolean ok = senhaService.verificar(senhaPura, u.getSenha());
        if (!ok) {
            log.warn("Login falhou: senha incorreta para {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário/senha inválidos");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", u.getId());
        claims.put("nome", u.getNome());
        claims.put("grupo", u.getGrupo() != null ? u.getGrupo().name() : null);

        String token = jwtService.generateToken(u.getEmail(), claims);
        Jws<Claims> parsed = jwtService.parse(token);
        Instant exp = parsed.getBody().getExpiration().toInstant();

        LoginResponse resp = new LoginResponse(
                token, exp, u.getNome(), u.getEmail(),
                u.getGrupo() != null ? u.getGrupo().name() : null
        );
        return ResponseEntity.ok(resp);
    }
}

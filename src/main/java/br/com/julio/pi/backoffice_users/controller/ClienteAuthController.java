package br.com.julio.pi.backoffice_users.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.dto.cliente.ClienteLoginDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.ClienteLoginResponse;
import br.com.julio.pi.backoffice_users.dto.cliente.ClienteRegisterDTO;
import br.com.julio.pi.backoffice_users.model.cliente.Cliente;
import br.com.julio.pi.backoffice_users.security.JwtService;
import br.com.julio.pi.backoffice_users.service.ClienteService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes/auth")

public class ClienteAuthController {

    private final ClienteService clientes;
    private final JwtService jwt;

    public ClienteAuthController(ClienteService clientes, JwtService jwt) {
        this.clientes = clientes;
        this.jwt = jwt;
    }

   
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ClienteRegisterDTO dto) {
        try {
            Cliente c = clientes.registrar(sanitizeRegister(dto));
            
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(err(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err("Falha ao registrar cliente"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ClienteLoginDTO dto) {
        try {
            String emailNorm = normalizeEmail(dto.email());
            Cliente c = clientes.login(emailNorm, dto.senha());

            var claims = new HashMap<String, Object>();
            claims.put("tipo", "CLIENTE");        // vini NAO MEXA AQUI!
            claims.put("cid", c.getId());         
            claims.put("nome", c.getNomeCompleto());

           
            String token = jwt.generateToken(c.getEmail(), claims);
            Jws<Claims> parsed = jwt.parse(token);
            Instant exp = parsed.getBody().getExpiration().toInstant();

            ClienteLoginResponse resp = new ClienteLoginResponse(
                    token,
                    exp,
                    c.getNomeCompleto(),
                    c.getEmail(),
                    "CLIENTE"
            );
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err("Falha no login do cliente"));
        }
    }

    private ClienteRegisterDTO sanitizeRegister(ClienteRegisterDTO d) {
        return new ClienteRegisterDTO(
                d.nomeCompleto(),
                normalizeEmail(d.email()),
                d.cpf(),
                d.dataNascimento(),
                d.genero(),
                d.senha(),
                d.confirmaSenha(),
                d.fatCep(),
                d.fatNumero(),
                d.fatComplemento(),
                d.entCep(),
                d.entNumero(),
                d.entComplemento()
        );
    }

    private String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase(Locale.ROOT);
    }

    private static record ErrorMsg(String detail) {}
    private ErrorMsg err(String msg) { return new ErrorMsg(msg == null ? "Erro" : msg); }
}

package br.com.julio.pi.backoffice_users.controller;

import java.time.Instant;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.julio.pi.backoffice_users.dto.cliente.*;
import br.com.julio.pi.backoffice_users.model.cliente.Cliente;
import br.com.julio.pi.backoffice_users.security.JwtService;
import br.com.julio.pi.backoffice_users.service.ClienteService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@RestController
@RequestMapping("/api/clientes/auth")
public class ClienteAuthController {

    private final ClienteService clientes;
    private final JwtService jwt;

    public ClienteAuthController(ClienteService clientes, JwtService jwt) {
        this.clientes = clientes; this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody ClienteRegisterDTO dto){
        Cliente c = clientes.registrar(dto);
        return ResponseEntity.ok().build(); // redireciona pro login pelo front
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ClienteLoginDTO dto){
        Cliente c = clientes.login(dto.email(), dto.senha());
        var claims = new HashMap<String,Object>();
        claims.put("tipo", "CLIENTE");
        claims.put("cid", c.getId());
        claims.put("nome", c.getNomeCompleto());
        String token = jwt.generateToken(c.getEmail(), claims);

        Jws<Claims> parsed = jwt.parse(token);
        Instant exp = parsed.getBody().getExpiration().toInstant();
        return ResponseEntity.ok(new ClienteLoginResponse(token, exp, c.getNomeCompleto(), c.getEmail(), "CLIENTE"));
    }
}

package br.com.julio.pi.backoffice_users.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.dto.cliente.AlterarSenhaDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.ClienteUpdateDTO;
import br.com.julio.pi.backoffice_users.dto.cliente.EnderecoCreateDTO;
import br.com.julio.pi.backoffice_users.model.cliente.EnderecoCliente;
import br.com.julio.pi.backoffice_users.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClientePerfilController {

    private final ClienteService clientes;

    public ClientePerfilController(ClienteService clientes) {
        this.clientes = clientes;
    }

    private Long currentId(HttpServletRequest req) {
        Object cid = req.getAttribute("CLIENT_ID");
        if (cid == null) {
            throw new RuntimeException("Não autenticado");
        }
        return (Long) cid;
    }

    // ===== Perfil =====

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        return ResponseEntity.ok(clientes.me(currentId(req)));
    }

    @PutMapping("/me")
    public ResponseEntity<?> atualizar(HttpServletRequest req,
                                       @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clientes.atualizarBasico(currentId(req), dto));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(HttpServletRequest req,
                                             @Valid @RequestBody AlterarSenhaDTO dto) {
        clientes.alterarSenha(currentId(req), dto);
        return ResponseEntity.ok().build();
    }

    // ===== Endereços =====

    @GetMapping("/enderecos")
    public List<EnderecoCliente> listarEnderecos(HttpServletRequest req) {
        return clientes.listarEnderecos(currentId(req));
    }

    @PostMapping("/enderecos")
    public EnderecoCliente adicionarEndereco(HttpServletRequest req,
                                             @Valid @RequestBody EnderecoCreateDTO dto) {
        return clientes.adicionarEndereco(currentId(req), dto);
    }

    @PatchMapping("/enderecos/{id}/padrao")
    public ResponseEntity<Void> setPadrao(HttpServletRequest req,
                                          @PathVariable Long id,
                                          @RequestParam boolean padrao) {
        clientes.setPadrao(currentId(req), id, padrao);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/enderecos/{id}")
    public ResponseEntity<Void> removerEndereco(HttpServletRequest req,
                                                @PathVariable Long id) {
        clientes.removerEndereco(currentId(req), id);
        return ResponseEntity.noContent().build();
    }
}

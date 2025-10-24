package br.com.julio.pi.backoffice_users.controller.cliente;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.julio.pi.backoffice_users.dto.cliente.AlterarSenhaDTO;

public class ClienteMeController {
    
    private ClienteMeController clienteService;

    @PutMapping("/senha")
public ResponseEntity<?> alterarSenha(
        @AuthenticationPrincipal AuthClientePrincipal me,
        @RequestBody AlterarSenhaDTO dto) {

    if (me == null) return ResponseEntity.status(401).build();
    clienteService.alterarSenha(me.getId(), dto);
    return ResponseEntity.ok().build();
}
        
}


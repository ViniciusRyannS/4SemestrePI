package br.com.julio.pi.backoffice_users.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.julio.pi.backoffice_users.model.pedido.Pedido;
import br.com.julio.pi.backoffice_users.repository.PedidoRepository;

@RestController
@RequestMapping("/api/clientes/pedidos")
public class ClientePedidoController {

    private final PedidoRepository pedidoRepo;

    public ClientePedidoController(PedidoRepository pedidoRepo) {
        this.pedidoRepo = pedidoRepo;
    }

    /** Lista pedidos do CLIENTE logado (via CLIENT_ID colocado pelo JwtAuthFilter). */
    @GetMapping
    public List<Pedido> listarDoCliente(
            @RequestAttribute(name = "CLIENT_ID", required = false) Long clienteId) {

        if (clienteId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não autenticado");
        }
        return pedidoRepo.findByClienteIdOrderByCriadoEmDesc(clienteId);
    }

    /** Busca um pedido específico do CLIENTE logado (checa propriedade). */
    @GetMapping("/{id}")
    public Pedido buscarDoCliente(@PathVariable Long id,
            @RequestAttribute(name = "CLIENT_ID", required = false) Long clienteId) {

        if (clienteId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não autenticado");
        }

        return pedidoRepo.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }
}

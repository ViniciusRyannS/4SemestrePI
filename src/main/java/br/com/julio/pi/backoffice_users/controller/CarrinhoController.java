package br.com.julio.pi.backoffice_users.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.cart.Carrinho;
import br.com.julio.pi.backoffice_users.dto.cliente.DadosCartaoDTO;
import br.com.julio.pi.backoffice_users.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping
    public Carrinho ver(HttpSession s) { return carrinhoService.verCarrinho(s); }

    @GetMapping("/resumo")
    public Carrinho resumo(HttpSession s) { return carrinhoService.verCarrinho(s); }

    @PostMapping("/itens")
    public Carrinho adicionarItem(HttpSession s,
                                  @RequestParam Long produtoId,
                                  @RequestParam(defaultValue = "1") int quantidade) {
        return carrinhoService.adicionarItem(s, produtoId, quantidade);
    }

    @PutMapping("/itens/{produtoId}")
    public Carrinho atualizarQuantidade(HttpSession s,
                                        @PathVariable Long produtoId,
                                        @RequestParam int quantidade) {
        return carrinhoService.atualizarQuantidade(s, produtoId, quantidade);
    }

    @DeleteMapping("/itens/{produtoId}")
    public Carrinho removerItem(HttpSession s, @PathVariable Long produtoId) {
        return carrinhoService.removerItem(s, produtoId);
    }

    @PostMapping("/frete")
    public Carrinho aplicarFrete(HttpSession s,
                                 @RequestParam String modalidade,
                                 @RequestParam BigDecimal valor) {
        return carrinhoService.aplicarFrete(s, modalidade, valor);
    }

    @PostMapping({"/endereco", "/entrega"})
    public Carrinho selecionarEndereco(HttpSession s,
                                       @RequestParam Long enderecoId,
                                       @RequestAttribute(name = "CLIENT_ID", required = false) Long clienteId) {
        return carrinhoService.selecionarEnderecoEntrega(s, enderecoId, clienteId);
    }

    /**
     add forma de pagamento, formaBoleto e parcelas do cartao
     */
    @PostMapping("/pagamento")
    public Carrinho selecionarPagamento(HttpSession session,
                                        @RequestParam String forma,
                                        @RequestParam(required = false) Integer parcelas,
                                        @RequestBody(required = false) DadosCartaoDTO body) {

        Integer parc = parcelas;
        if (parc == null && body != null) parc = body.parcelas();

        DadosCartaoDTO dto = null;
        if ("CARTAO".equalsIgnoreCase(forma)) {
            dto = new DadosCartaoDTO(
                body != null ? body.numero()   : null,
                body != null ? body.nome()     : null,
                body != null ? body.validade() : null,
                body != null ? body.cvv()      : null,
                parc
            );
        }
        return carrinhoService.selecionarPagamento(session, forma, dto);
    }

    @DeleteMapping
    public ResponseEntity<Void> limpar(HttpSession s) {
        carrinhoService.limpar(s);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/finalizar")
public ResponseEntity<Map<String, Object>> finalizar(
        HttpSession session,
        @RequestAttribute(name = "CLIENT_ID", required = false) Long clienteId) {

    if (clienteId == null) {
        return ResponseEntity.status(401).build();
    }

    var recibo = carrinhoService.finalizar(session, clienteId);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id",    recibo.getPedidoId());   
    body.put("total", recibo.getTotal());
    body.put("status",  "AGUARDANDO_PAGAMENTO");
    body.put("message", "Pedido criado com sucesso");

    return ResponseEntity.ok(body);
}

}

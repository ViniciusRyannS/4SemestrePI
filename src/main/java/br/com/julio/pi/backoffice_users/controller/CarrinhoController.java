package br.com.julio.pi.backoffice_users.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.cart.Carrinho;
import br.com.julio.pi.backoffice_users.cart.ReciboCompra;
import br.com.julio.pi.backoffice_users.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/carrinho")
// @CrossOrigin(origins = "*") // descomente se o front estiver em outra origem
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    /** Retorna o carrinho atual da sessão. */
    @GetMapping
    public Carrinho ver(HttpSession session) {
        return carrinhoService.verCarrinho(session);
    }

    /** Adiciona (ou soma) um item ao carrinho. */
    @PostMapping("/itens")
    public Carrinho adicionarItem(HttpSession session,
                                  @RequestParam Long produtoId,
                                  @RequestParam(defaultValue = "1") int quantidade) {
        return carrinhoService.adicionarItem(session, produtoId, quantidade);
    }

    /** Atualiza a quantidade de um item; se <=0, remove. */
    @PutMapping("/itens/{produtoId}")
    public Carrinho atualizarQuantidade(HttpSession session,
                                        @PathVariable Long produtoId,
                                        @RequestParam int quantidade) {
        return carrinhoService.atualizarQuantidade(session, produtoId, quantidade);
    }

    /** Remove item do carrinho. */
    @DeleteMapping("/itens/{produtoId}")
    public Carrinho removerItem(HttpSession session, @PathVariable Long produtoId) {
        return carrinhoService.removerItem(session, produtoId);
    }

    /** Aplica a modalidade de frete e valor no carrinho. */
    @PostMapping("/frete")
    public Carrinho aplicarFrete(HttpSession session,
                                 @RequestParam String modalidade,
                                 @RequestParam BigDecimal valor) {
        return carrinhoService.aplicarFrete(session, modalidade, valor);
    }

    @PostMapping("/finalizar")
    public ReciboCompra finalizar(HttpSession session) {
    return carrinhoService.finalizar(session);
}


}

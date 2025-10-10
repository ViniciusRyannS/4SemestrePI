package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.julio.pi.backoffice_users.cart.Carrinho;
import br.com.julio.pi.backoffice_users.cart.ReciboCompra;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CarrinhoService {

    public static final String CART_ATTR = "CART";

    private final ProdutoRepository produtoRepository;

    public CarrinhoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /** Retorna o carrinho da sessão (cria se não existir). */
    public Carrinho verCarrinho(HttpSession session) {
        Carrinho c = (Carrinho) session.getAttribute(CART_ATTR);
        if (c == null) {
            c = new Carrinho();
            session.setAttribute(CART_ATTR, c);
        }
        return c;
    }

    /** Adiciona (ou soma) item ao carrinho a partir do produtoId. */
    public Carrinho adicionarItem(HttpSession session, Long produtoId, int quantidade) {
        if (produtoId == null || quantidade <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto e quantidade devem ser válidos");
        }
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Carrinho c = verCarrinho(session);
        c.adicionarOuSomar(p.getId(), p.getNome(), p.getPreco(), quantidade);
        // Se quiser invalidar frete ao alterar itens, descomente:
        // c.aplicarFrete(null, BigDecimal.ZERO);
        return c;
    }

    /** Atualiza a quantidade do item; se novaQtd <= 0, remove. */
    public Carrinho atualizarQuantidade(HttpSession session, Long produtoId, int novaQtd) {
        if (produtoId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido");
        Carrinho c = verCarrinho(session);
        c.atualizarQuantidade(produtoId, novaQtd);
        return c;
    }

    /** Remove item do carrinho. */
    public Carrinho removerItem(HttpSession session, Long produtoId) {
        if (produtoId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido");
        Carrinho c = verCarrinho(session);
        c.remover(produtoId);
        return c;
    }

    /** Aplica uma modalidade de frete no carrinho. */
    public Carrinho aplicarFrete(HttpSession session, String modalidade, BigDecimal valor) {
        if (modalidade == null || modalidade.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modalidade de frete é obrigatória");
        }
        if (valor == null || valor.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor de frete inválido");
        }
        Carrinho c = verCarrinho(session);
        c.aplicarFrete(modalidade, valor);
        return c;
    }

    /** Zera o carrinho na sessão (se precisar). */
    public void limpar(HttpSession session) {
        session.removeAttribute(CART_ATTR);
    }

    // ========== NOVO: Finalizar compra (gera recibo e limpa sessão) ==========
    public ReciboCompra finalizar(HttpSession session) {
        Carrinho c = verCarrinho(session);

        if (c.getItens().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");
        }

        var itensRecibo = c.getItens().stream()
                .map(i -> new ReciboCompra.Item(
                        i.getProdutoId(),
                        i.getNome(),
                        i.getPreco(),
                        i.getQuantidade(),
                        i.getTotalLinha()))
                .collect(Collectors.toList());

        String pedidoId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ReciboCompra recibo = new ReciboCompra(
                pedidoId,
                OffsetDateTime.now(),
                itensRecibo,
                c.getModalidadeFrete(),
                c.getSubtotal(),
                c.getFrete(),
                c.getTotal()
        );

        // Limpa a sessão após gerar o recibo
        limpar(session);
        return recibo;
    }
}

package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.julio.pi.backoffice_users.cart.Carrinho;
import br.com.julio.pi.backoffice_users.cart.ReciboCompra;
import br.com.julio.pi.backoffice_users.dto.cliente.DadosCartaoDTO;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.cliente.EnderecoCliente;
import br.com.julio.pi.backoffice_users.model.pedido.Pedido;
import br.com.julio.pi.backoffice_users.model.pedido.PedidoItem;
import br.com.julio.pi.backoffice_users.repository.EnderecoClienteRepository;
import br.com.julio.pi.backoffice_users.repository.PedidoRepository;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CarrinhoService {

    public static final String CART_ATTR = "CART";

    private final ProdutoRepository produtoRepository;
    private final EnderecoClienteRepository endRepo;
    private final PedidoRepository pedidoRepo;

    public CarrinhoService(ProdutoRepository produtoRepository,
                           EnderecoClienteRepository endRepo,
                           PedidoRepository pedidoRepo) {
        this.produtoRepository = produtoRepository;
        this.endRepo = endRepo;
        this.pedidoRepo = pedidoRepo;
    }

    public Carrinho verCarrinho(HttpSession session) {
        Carrinho c = (Carrinho) session.getAttribute(CART_ATTR);
        if (c == null) {
            c = new Carrinho();
            session.setAttribute(CART_ATTR, c);
        }
        return c;
    }

    public Carrinho adicionarItem(HttpSession s, Long produtoId, int quantidade) {
        if (produtoId == null || quantidade <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto e quantidade devem ser válidos");

        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Carrinho c = verCarrinho(s);
        c.adicionarOuSomar(p.getId(), p.getNome(), p.getPreco(), quantidade);
        return c;
    }

    public Carrinho atualizarQuantidade(HttpSession s, Long produtoId, int novaQtd) {
        if (produtoId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido");
        Carrinho c = verCarrinho(s);
        c.atualizarQuantidade(produtoId, novaQtd);
        return c;
    }

    public Carrinho removerItem(HttpSession s, Long produtoId) {
        if (produtoId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido");
        Carrinho c = verCarrinho(s);
        c.remover(produtoId);
        return c;
    }

    public Carrinho aplicarFrete(HttpSession s, String modalidade, BigDecimal valor) {
        if (modalidade == null || modalidade.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modalidade de frete é obrigatória");
        if (valor == null || valor.signum() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor de frete inválido");

        Carrinho c = verCarrinho(s);
        if (c.getItens().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");

        c.aplicarFrete(modalidade, valor);
        return c;
    }

    // substitua o método antigo por este (mantém overload sem clienteId se você quiser)
public Carrinho selecionarEnderecoEntrega(HttpSession s, Long enderecoId, Long clienteId) {
    if (enderecoId == null)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço inválido");

    var end = endRepo.findById(enderecoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço não encontrado"));

    // Se temos o CLIENT_ID do token do cliente, garanta que o endereço é dele
    if (clienteId != null && (end.getCliente() == null || !clienteId.equals(end.getCliente().getId()))) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Endereço não pertence ao cliente autenticado");
    }

    Carrinho c = verCarrinho(s);
    c.setEnderecoEntregaId(enderecoId);
    return c;
}

// (opcional) mantenha o antigo por compatibilidade interna
public Carrinho selecionarEnderecoEntrega(HttpSession s, Long enderecoId) {
    return selecionarEnderecoEntrega(s, enderecoId, null);
}


   public Carrinho selecionarPagamento(HttpSession session, String forma, DadosCartaoDTO cartao) {
    var c = verCarrinho(session);

    if (c.getItens().isEmpty())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");

    if (c.getEnderecoEntregaId() == null)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o endereço de entrega");

    if (c.getModalidadeFrete() == null || c.getFrete() == null)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o frete antes do pagamento");

    if ("BOLETO".equalsIgnoreCase(forma)) {
        c.setFormaPagamento("BOLETO");
        return c;
    }

    if (!"CARTAO".equalsIgnoreCase(forma))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento inválida");

    // -------- CARTÃO: aceitar apenas parcelas (simulação) ----------
    Integer parcelas = (cartao != null ? cartao.parcelas() : null);
    if (parcelas == null || parcelas < 1)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe as parcelas do cartão");

    c.setFormaPagamento("CARTAO"); // não armazenamos dados sensíveis
    return c;
}

private boolean isBlank(String s){ return s == null || s.isBlank(); }


    public void limpar(HttpSession session) {
        session.removeAttribute(CART_ATTR);
    }

    /** Finaliza: exige endereço + frete + pagamento; cria Pedido (AGUARDANDO_PAGAMENTO). */
    public ReciboCompra finalizar(HttpSession s, Long clienteId) {
        Carrinho c = verCarrinho(s);

        if (c.getItens().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");
        if (c.getEnderecoEntregaId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um endereço de entrega");
        if (c.getModalidadeFrete() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma modalidade de frete");
        if (c.getFormaPagamento() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma forma de pagamento");

        // snapshot do endereço
        EnderecoCliente e = endRepo.findById(c.getEnderecoEntregaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço inválido"));

        Pedido p = new Pedido();
        p.setClienteId(clienteId);
        p.setSubtotal(c.getSubtotal());
        p.setFrete(c.getFrete());
        p.setTotal(c.getTotal());
        p.setModalidadeFrete(c.getModalidadeFrete());
        p.setFormaPagamento(c.getFormaPagamento());
        // parcelas: não guardadas no carrinho; opcionais -> 1
        p.setParcelas(1);

        p.setEndLogradouro(e.getLogradouro());
        p.setEndNumero(e.getNumero());
        p.setEndComplemento(e.getComplemento());
        p.setEndBairro(e.getBairro());
        p.setEndCidade(e.getCidade());
        p.setEndUf(e.getUf());
        p.setEndCep(e.getCep());

        c.getItens().forEach(i -> {
            var it = new PedidoItem();
            it.setPedido(p);
            it.setProdutoId(i.getProdutoId());
            it.setNome(i.getNome());
            it.setPreco(i.getPreco());
            it.setQuantidade(i.getQuantidade());
            it.setTotalLinha(i.getTotalLinha());
            p.getItens().add(it);
        });

        pedidoRepo.save(p);

        var itensRecibo = p.getItens().stream()
                .map(i -> new ReciboCompra.Item(i.getProdutoId(), i.getNome(), i.getPreco(), i.getQuantidade(), i.getTotalLinha()))
                .collect(Collectors.toList());

        ReciboCompra recibo = new ReciboCompra(
                String.valueOf(p.getId()),
                OffsetDateTime.now(),
                itensRecibo,
                p.getModalidadeFrete(),
                p.getSubtotal(),
                p.getFrete(),
                p.getTotal(),
                c.getEnderecoEntregaId(),
                p.getFormaPagamento()
        );

        limpar(s);
        return recibo;
    }
}


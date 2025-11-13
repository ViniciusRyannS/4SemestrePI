package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // -------- Endereço de entrega --------
    public Carrinho selecionarEnderecoEntrega(HttpSession s, Long enderecoId, Long clienteId) {
        if (enderecoId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço inválido");

        var end = endRepo.findById(enderecoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço não encontrado"));

        if (clienteId != null && (end.getCliente() == null || !clienteId.equals(end.getCliente().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Endereço não pertence ao cliente autenticado");
        }

        Carrinho c = verCarrinho(s);
        c.setEnderecoEntregaId(enderecoId);
        return c;
    }

    public Carrinho selecionarEnderecoEntrega(HttpSession s, Long enderecoId) {
        return selecionarEnderecoEntrega(s, enderecoId, null);
    }

    // -------- Pagamento --------
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

    /** Finaliza: exige endereço + frete + pagamento; cria Pedido e gera código. */
    @Transactional
    public ReciboCompra finalizar(HttpSession s, Long clienteId) {
        Carrinho c = verCarrinho(s);

        if (c.getItens().isEmpty())           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio");
        if (c.getEnderecoEntregaId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um endereço de entrega");
        if (c.getModalidadeFrete() == null)   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma modalidade de frete");
        if (c.getFormaPagamento() == null)    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma forma de pagamento");

        EnderecoCliente e = endRepo.findById(c.getEnderecoEntregaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço inválido"));

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setSubtotal(c.getSubtotal());
        pedido.setFrete(c.getFrete());
        pedido.setTotal(c.getTotal());
        pedido.setModalidadeFrete(c.getModalidadeFrete());
        pedido.setFormaPagamento(c.getFormaPagamento());
        pedido.setParcelas(1);

        pedido.setEndLogradouro(e.getLogradouro());
        pedido.setEndNumero(e.getNumero());
        pedido.setEndComplemento(e.getComplemento());
        pedido.setEndBairro(e.getBairro());
        pedido.setEndCidade(e.getCidade());
        pedido.setEndUf(e.getUf());
        pedido.setEndCep(e.getCep());

        for (var i : c.getItens()) {
            PedidoItem it = new PedidoItem();
            it.setPedido(pedido);
            it.setProdutoId(i.getProdutoId());
            it.setNome(i.getNome());
            it.setPreco(i.getPreco());
            it.setQuantidade(i.getQuantidade());
            it.setTotalLinha(i.getTotalLinha());
            pedido.getItens().add(it);
        }

        pedido = pedidoRepo.save(pedido);

        String codigo = "PO-" + String.format("%06d", pedido.getId());
        pedido.setCodigo(codigo);
        pedido = pedidoRepo.save(pedido);

        var itensRecibo = pedido.getItens().stream()
                .map(i -> new ReciboCompra.Item(i.getProdutoId(), i.getNome(), i.getPreco(), i.getQuantidade(), i.getTotalLinha()))
                .collect(Collectors.toList());

        ReciboCompra recibo = new ReciboCompra(
                String.valueOf(pedido.getId()),
                pedido.getCodigo(),                 
                OffsetDateTime.now(),
                itensRecibo,
                pedido.getModalidadeFrete(),
                pedido.getSubtotal(),
                pedido.getFrete(),
                pedido.getTotal(),
                c.getEnderecoEntregaId(),
                pedido.getFormaPagamento()
        );

        limpar(s);
        return recibo;
    }
}

package br.com.julio.pi.backoffice_users.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;
import br.com.julio.pi.backoffice_users.service.ProdutoService;

@Configuration
public class ProdutoSeeder implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;

    public ProdutoSeeder(ProdutoRepository produtoRepository, ProdutoService produtoService) {
        this.produtoRepository = produtoRepository;
        this.produtoService = produtoService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (produtoRepository.count() > 0) return; 

        Produto p1 = new Produto("Goma de mascar", new BigDecimal("0.50"), 77,
                "Sabor menta refrescante", new BigDecimal("3.5"), StatusProduto.ATIVO);
        p1 = produtoService.incluir(p1);
        produtoService.adicionarImagem(p1.getId(), "/imagens/1/imagem1.jpg", true);

        Produto p2 = new Produto("Jogo de Xadrez", new BigDecimal("843.50"), 34,
                "Jogo completo com tabuleiro e peças", new BigDecimal("4.5"), StatusProduto.ATIVO);
        p2 = produtoService.incluir(p2);
        produtoService.adicionarImagem(p2.getId(), "/imagens/2/box.jpg", true);

        Produto p3 = new Produto("Baralho", new BigDecimal("10.00"), 35,
                "Baralho padrão 52 cartas", new BigDecimal("4.0"), StatusProduto.INATIVO);
        p3 = produtoService.incluir(p3);
        produtoService.adicionarImagem(p3.getId(), "/imagens/3/front.jpg", true);
    }
}

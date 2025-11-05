package br.com.julio.pi.backoffice_users.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(ProdutoRepository repo) {
        return args -> {
            if (repo.count() == 0) { // só se estiver vazio
                repo.save(new Produto(
                    "Notebook Asus Classic",
                    new BigDecimal("2000.00"),
                    10,
                    "Notebook com configuração para o dia a dia",
                    new BigDecimal("4.0"),
                    StatusProduto.ATIVO
                ));
                repo.save(new Produto(
                    "Smartphone Galaxy X",
                    new BigDecimal("2500.00"),
                    3,
                    "Celular com bom custo-benefício",
                    new BigDecimal("4.5"),
                    StatusProduto.ATIVO
                ));
            }
        };
    }
}

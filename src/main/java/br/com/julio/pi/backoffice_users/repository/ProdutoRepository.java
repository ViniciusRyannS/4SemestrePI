package br.com.julio.pi.backoffice_users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCaseOrderByIdDesc(String nome);
    List<Produto> findByStatusOrderByIdDesc(StatusProduto status);
    List<Produto> findAllByOrderByIdDesc();
}

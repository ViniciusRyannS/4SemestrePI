package br.com.julio.pi.backoffice_users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.julio.pi.backoffice_users.model.ImagemProduto;

public interface ImagemProdutoRepository extends JpaRepository<ImagemProduto, Long> {
    Optional<ImagemProduto> findFirstByProdutoIdAndPrincipalTrue(Long produtoId);
    Optional<ImagemProduto> findFirstByProdutoIdOrderByIdAsc(Long produtoId);
    List<ImagemProduto> findByProdutoIdOrderByPrincipalDescIdAsc(Long produtoId);
    List<ImagemProduto> findByProdutoIdOrderByIdAsc(Long produtoId);

    long countByProdutoId(Long produtoId);
}
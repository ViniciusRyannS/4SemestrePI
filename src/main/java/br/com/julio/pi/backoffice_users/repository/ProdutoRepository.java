package br.com.julio.pi.backoffice_users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.julio.pi.backoffice_users.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCaseOrderByIdDesc(String nome);
    List<Produto> findAllByOrderByIdDesc();

    @Query("""
           select distinct p
           from Produto p
           left join fetch p.imagens
           where p.id = :id
           """)
    Optional<Produto> findByIdFetchImagens(@Param("id") Long id);
}

package br.com.julio.pi.backoffice_users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.julio.pi.backoffice_users.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca por nome (case-insensitive) ordenando pelo id desc
    List<Produto> findByNomeContainingIgnoreCaseOrderByIdDesc(String nome);

    // Lista tudo ordenando pelo id desc
    List<Produto> findAllByOrderByIdDesc();

    // Carrega o produto com as imagens inicializadas (evita LazyInitializationException)
    // DISTINCT evita duplicatas quando há múltiplas imagens no fetch join
    @Query("""
           select distinct p
           from Produto p
           left join fetch p.imagens
           where p.id = :id
           """)
    Optional<Produto> findByIdFetchImagens(@Param("id") Long id);
}

package br.senac.sp.produto.repository;

import br.senac.sp.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends MongoRepository<Produto, String>{ //extends JpaRepository<Produto, Long> {

    List<Produto> findByLote(String lote);
}

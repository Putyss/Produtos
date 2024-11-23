package br.senac.sp.produto.controller;

import br.senac.sp.produto.model.Produto;
import br.senac.sp.produto.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    private  ProdutoController(ProdutoRepository repository){
        this.produtoRepository = repository;
    }
    @GetMapping("/get-produtos")
    public ResponseEntity<List<Produto>> recuperarTodos(){
        List<Produto> produtos = produtoRepository.findAll();
        System.out.println("Total de Produtos " + produtos.size());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/get-produto/{idProduto}")
    public ResponseEntity<Produto> recuperarPorId(@PathVariable(name = "idProduto") Long id){
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RuntimeException("ID NAO LOCALIZADO"));
        System.out.println(produto);
        return ResponseEntity.ok(produto);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Produto> cadastrar(@RequestBody ProdutoRequest request){
        var p = new Produto().setDescricao(request.getDescricao()).setPreco(request.getPreco()).
                setCodigoBarra(request.getCodigoBarra())
                .setLote(request.getLote()).setQuantidade(request.getQuantidade());
        var produtoCriado = produtoRepository.save(p);
        System.out.println(produtoCriado);
        return ResponseEntity.ok(produtoCriado);
    }

    @PutMapping("/atualizar/{idProduto}")
    public  ResponseEntity<Produto> alterarProdutoTotal(
            @PathVariable(name = "idProduto" ) Long id, @RequestBody ProdutoRequest request){

        if(Objects.isNull(request.getDescricao()) ||
                Objects.isNull(request.getPreco()) ||
                Objects.isNull(request.getQuantidade()) ||
                Objects.isNull(request.getLote()) ||
                Objects.isNull(request.getCodigoBarra())
        ){
            throw new RuntimeException("OS ATRIBUTOS NAO PODEM SER NULOS");
        }

        Produto p = new Produto();

        var produtoOptional = produtoRepository.findById(id);
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        p.setId(id);
        p.setDescricao(request.getDescricao());
        p.setCodigoBarra(request.getCodigoBarra());
        p.setLote(request.getLote());
        p.setPreco(request.getPreco());
        p.setQuantidade(request.getQuantidade());

        Produto produtoSalvo = produtoRepository.save(p);
        return ResponseEntity.ok().body(produtoSalvo);
    }

    @PatchMapping("/atualizar/{idProduto}")
    public  ResponseEntity<Produto> alterarProdutoParcial(
            @PathVariable(name = "idProduto" ) Long id, @RequestBody ProdutoRequest request){

        Produto produtoEntidade = new Produto();

        var produtoOptional = produtoRepository.findById(id);
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        var produtoBancoDados = produtoOptional.get();

        produtoEntidade.setId(id);

        produtoEntidade.setDescricao(Objects.isNull(request.getDescricao()) ?
                produtoBancoDados.getDescricao() : request.getDescricao());

        produtoEntidade.setLote(Objects.isNull(request.getLote()) ?
                produtoBancoDados.getLote() : request.getLote());

        produtoEntidade.setPreco(Objects.isNull(request.getPreco()) ?
                produtoBancoDados.getPreco() : request.getPreco());

        produtoEntidade.setQuantidade(Objects.isNull(request.getQuantidade()) ?
                produtoBancoDados.getQuantidade() : request.getQuantidade());

        produtoEntidade.setCodigoBarra(Objects.isNull(request.getCodigoBarra()) ?
                produtoBancoDados.getCodigoBarra() : request.getCodigoBarra());

        Produto produtoAtualizado = produtoRepository.save(produtoEntidade);

        return ResponseEntity.ok().body(produtoAtualizado);

    }

    @DeleteMapping("/deletar/{idProduto}")
    public ResponseEntity<Void> deletar(@PathVariable(name = "idProduto")Long id){

        var produtoOptional = produtoRepository.findById(id);
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        produtoRepository.delete(produtoOptional.get());

        return  ResponseEntity.noContent().build();

    }

    @DeleteMapping("/deletarTudo")
    public  ResponseEntity<Void> deletarTudo(){

        produtoRepository.deleteAll();

        return  ResponseEntity.noContent().build();

    }

    @GetMapping("paginador")
    public ResponseEntity<Page<Produto>> getProdutosPaginado(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int itens,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String ordem
    ){

        var ordenacao = ordem.equalsIgnoreCase("asc") ? Sort.by(ordenarPor).ascending() :
                Sort.by(ordenarPor).descending();

        var paginador = PageRequest.of(pagina, itens, ordenacao);

        var produtosPaginado = produtoRepository.findAll(paginador);

        return ResponseEntity.ok().body(produtosPaginado);

    }

    @GetMapping("/somar-precos/{lote}")
    public ResponseEntity<BigDecimal> somarPrecosPorLote(@PathVariable(name = "lote") String lote) {

        var produtosDoLote = produtoRepository.findByLote(lote);


        if (produtosDoLote.isEmpty()) {
            throw  new RuntimeException("LOTE NAO EXISTE");
        }


        BigDecimal somaPrecos = produtosDoLote.stream()
                .map(Produto::getPreco)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(somaPrecos);
    }

}

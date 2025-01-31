package br.senac.sp.produto.controller.api;

import br.senac.sp.produto.controller.ProdutoRequest;
import br.senac.sp.produto.model.Produto;
import br.senac.sp.produto.repository.ProdutoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("produtos")
@Tag(name = "API - Produto Controller API",
        description = "Controller para tratar requisições de Produtos na API")
public class ProdutoControllerApi {

    private final ProdutoRepository produtoRepository;

    private ProdutoControllerApi(ProdutoRepository repository){
        this.produtoRepository = repository;
    }
    @GetMapping("/get-produtos")
    @Operation(summary = "Recuperar Todos",
            description = "Retorna todos os produtos")
    public ResponseEntity<List<Produto>> recuperarTodos(){
        List<Produto> produtos = produtoRepository.findAll();
        System.out.println("Total de Produtos " + produtos.size());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/get-produto/{idProduto}")
    @Operation(summary = "Recuperar por ID",
            description = "Recupera produto por ID")
    public ResponseEntity<Produto> recuperarPorId(@PathVariable(name = "idProduto") String id){
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RuntimeException("ID NAO LOCALIZADO"));
        System.out.println(produto);
        return ResponseEntity.ok(produto);
    }

    @PostMapping("/cadastrar")
    @Operation(summary = "Cadastrar produtos",
            description = "Cadastra novos produtos")
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody ProdutoRequest request){
        var p = new Produto().setDescricao(request.getDescricao()).setPreco(request.getPreco()).
                setCodigoBarra(request.getCodigoBarra())
                .setLote(request.getLote()).setQuantidade(request.getQuantidade());
        var produtoCriado = produtoRepository.save(p);
        System.out.println(produtoCriado);
        return ResponseEntity.ok(produtoCriado);
    }

    @PutMapping("/atualizar/{idProduto}")
    @Operation(summary = "Alterar todos produtos",
            description = "Altera todos os dados dos produtos")
    public  ResponseEntity<Produto> alterarProdutoTotal(
            @PathVariable(name = "idProduto" ) String id, @RequestBody ProdutoRequest request){

        if(Objects.isNull(request.getDescricao()) ||
                Objects.isNull(request.getPreco()) ||
                Objects.isNull(request.getQuantidade()) ||
                Objects.isNull(request.getLote()) ||
                Objects.isNull(request.getCodigoBarra())
        ){
            throw new RuntimeException("OS ATRIBUTOS NAO PODEM SER NULOS");
        }

        Produto p = new Produto();

        var produtoOptional = produtoRepository.findById(String.valueOf(id));
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        p.setId(String.valueOf(id));
        p.setDescricao(request.getDescricao());
        p.setCodigoBarra(request.getCodigoBarra());
        p.setLote(request.getLote());
        p.setPreco(request.getPreco());
        p.setQuantidade(request.getQuantidade());

        Produto produtoSalvo = produtoRepository.save(p);
        return ResponseEntity.ok().body(produtoSalvo);
    }

    @PatchMapping("/atualizar/{idProduto}")
    @Operation(summary = "Alterar produtos",
            description = "Altera dados parcial dos produtos")
    public  ResponseEntity<Produto> alterarProdutoParcial(
            @PathVariable(name = "idProduto" ) String id, @RequestBody ProdutoRequest request){

        Produto produtoEntidade = new Produto();

        var produtoOptional = produtoRepository.findById(id);
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        var produtoBancoDados = produtoOptional.get();

        produtoEntidade.setId(String.valueOf(id));

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
    @Operation(summary = "Deletar",
            description = "Deletar produtos")
    public ResponseEntity<Void> deletar(@PathVariable(name = "idProduto")String id){

        var produtoOptional = produtoRepository.findById(id);
        if (produtoOptional.isEmpty()){
            throw  new RuntimeException("PRODUTO NAO EXISTE");
        }

        produtoRepository.delete(produtoOptional.get());

        return  ResponseEntity.noContent().build();

    }

    @DeleteMapping("/deletarTudo")
    @Operation(summary = "Deletar Todos",
            description = "Deleta todos os produtos")
    public  ResponseEntity<Void> deletarTudo(){

        produtoRepository.deleteAll();

        return  ResponseEntity.noContent().build();

    }

    @GetMapping("paginador")
    @Operation(summary = "Paginação",
            description = "Retorna produtos com paginação")
    public ResponseEntity<Page<Produto>> getProdutosPaginado(
            @Parameter(description = "Numero da pagina", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Quantidade de itens na pagina", example = "10")
            @RequestParam(defaultValue = "10") int itens,
            @Parameter(description = "Atributo que sera ordenado", example = "id")
            @RequestParam(defaultValue = "id") String ordenarPor,
            @Parameter(description = "Ordem da ordenação", example = "asc")
            @RequestParam(defaultValue = "asc") String ordem
    ){

        var ordenacao = ordem.equalsIgnoreCase("asc") ? Sort.by(ordenarPor).ascending() :
                Sort.by(ordenarPor).descending();

        var paginador = PageRequest.of(pagina, itens, ordenacao);

        var produtosPaginado = produtoRepository.findAll(paginador);

        return ResponseEntity.ok().body(produtosPaginado);

    }

    @GetMapping("/somar-precos/{lote}")
    @Operation(summary = "Somar preços",
            description = "Soma preço dos produtos por lote")
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

    @Operation(summary = "Adivinhar numero",
            description = "Tende descobrir numero certo de 0 a 10")
    @PostMapping("/validar-numero/{num}")
    public ResponseEntity<String> validarNum(@PathVariable("num")Integer numero){
        int numeroGerado = (int) (Math.random() * 10);
        if (numero.equals(numeroGerado)){
            return ResponseEntity.ok("ACERTOU");
        }

        throw  new RuntimeException("ERROU, o numero certo era " + numeroGerado);
    }

    @PostMapping("/set-cookie")
    public String setCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("username", "john_doe");
        cookie.setMaxAge(2 * 60); // cookie.setMaxAge(24 * 60 * 60); // Expira em 1 dia
        cookie.setHttpOnly(true); // Apenas acessível via HTTP (não disponível para JS)
        cookie.setPath("/"); // Disponível para toda a aplicação
        response.addCookie(cookie);
        return "Cookie configurado com sucesso!";
    }


    @GetMapping("/get-cookie")
    public String getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    return "Cookie encontrado: " + cookie.getValue();
                }
            }
        }
        return "Nenhum cookie encontrado";
    }

}

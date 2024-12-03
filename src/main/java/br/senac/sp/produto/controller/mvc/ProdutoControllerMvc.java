package br.senac.sp.produto.controller.mvc;

import br.senac.sp.produto.controller.ProdutoRequest;
import br.senac.sp.produto.model.Produto;
import br.senac.sp.produto.repository.ProdutoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("produtos")
@Tag(name = "Produto Controller MVC",
        description = "Controller para Produtos na API")
public class ProdutoControllerMvc {

    private final ProdutoRepository produtoRepository;

    public ProdutoControllerMvc(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Operation(summary = "Index",
            description = "Retorna o index dos produtos.")
    @GetMapping
    public String exibirPaginaInicial(Model model){
        return "index";
    }

    @GetMapping("/listar")
    @Operation(summary = "Exibir produtos",
            description = "Retorna todos os produtos exitente")
    public String exibirProdutos(
            @RequestParam(defaultValue = "0") int page, // Página atual
            @RequestParam(defaultValue = "10") int size, // Tamanho da página
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Produto> produtosPage = produtoRepository.findAll(pageable);

        int totalPages = produtosPage.getTotalPages();
        int currentPage = page;

        // Lógica para limitar os números de páginas visíveis
        int maxVisiblePages = 5; // Número máximo de páginas visíveis
        int startPage = Math.max(0, currentPage - (maxVisiblePages / 2));
        int endPage = Math.min(totalPages, startPage + maxVisiblePages);

        if (endPage - startPage < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages);
        }

        model.addAttribute("produtos", produtosPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "listaProdutos";
    }

    @PostMapping("/salvar")
    @Operation(summary = "Salvar",
            description = "Salva o produto")
    public String salvarProduto(@ModelAttribute ProdutoRequest request, Model model){
        var p = new Produto().setDescricao(request.getDescricao())
                .setPreco(request.getPreco())
                .setCodigoBarra(request.getCodigoBarra())
                .setLote(request.getLote())
                .setQuantidade(request.getQuantidade());

        produtoRepository.save(p);
        model.addAttribute("produtoSalvo", request);
        return "sucesso";
    }

    @GetMapping("/cadastro")
    @Operation(summary = "Cadastrar",
            description = "Cadastrar novos produtos")
    public String exibirFormulario(Model model){
        var request = new ProdutoRequest();
        model.addAttribute("produtoRequest", request);
        return "formularioProduto";
    }

}

package br.com.stockflow.controller;

import br.com.stockflow.dto.ProdutoCadastroDTO;
import br.com.stockflow.dto.ProdutoRespostaDTO;
import br.com.stockflow.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoRespostaDTO cadastrar(
            @Valid @RequestBody ProdutoCadastroDTO dto) {
        return produtoService.cadastrar(dto);
    }

    @GetMapping
    public List<ProdutoRespostaDTO> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ProdutoRespostaDTO buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    @PatchMapping("/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        produtoService.desativar(id);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long id) {
        produtoService.ativar(id);
    }
}

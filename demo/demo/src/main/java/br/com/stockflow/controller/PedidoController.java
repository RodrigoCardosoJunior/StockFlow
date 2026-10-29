package br.com.stockflow.controller;

import br.com.stockflow.dto.PedidoCadastroDTO;
import br.com.stockflow.dto.PedidoItemCadastroDTO;
import br.com.stockflow.dto.PedidoItemRespostaDTO;
import br.com.stockflow.dto.PedidoRespostaDTO;
import br.com.stockflow.entity.StatusPedido;
import br.com.stockflow.service.PedidoItemService;
import br.com.stockflow.service.PedidoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;

    public PedidoController(
            PedidoService pedidoService,
            PedidoItemService pedidoItemService) {
        this.pedidoService = pedidoService;
        this.pedidoItemService = pedidoItemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoRespostaDTO criar(
            @Valid @RequestBody PedidoCadastroDTO dto) {
        return pedidoService.criar(dto);
    }

    @GetMapping
    public List<PedidoRespostaDTO> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/{pedidoId}")
    public PedidoRespostaDTO buscarPorId(
            @PathVariable Long pedidoId) {
        return pedidoService.buscarPorId(pedidoId);
    }

    @PatchMapping("/{pedidoId}/status")
    public PedidoRespostaDTO alterarStatus(
            @PathVariable Long pedidoId,
            @RequestParam @NotNull StatusPedido novoStatus) {
        return pedidoService.alterarStatus(pedidoId, novoStatus);
    }

    @PostMapping("/{pedidoId}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoItemRespostaDTO adicionarItem(
            @PathVariable Long pedidoId,
            @Valid @RequestBody PedidoItemCadastroDTO dto) {
        return pedidoItemService.adicionar(pedidoId, dto);
    }

    @GetMapping("/{pedidoId}/itens")
    public List<PedidoItemRespostaDTO> listarItens(
            @PathVariable Long pedidoId) {
        return pedidoItemService.listarPorPedido(pedidoId);
    }

    @DeleteMapping("/{pedidoId}/itens/{produtoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerItem(
            @PathVariable Long pedidoId,
            @PathVariable Long produtoId) {
        pedidoItemService.remover(pedidoId, produtoId);
    }
}

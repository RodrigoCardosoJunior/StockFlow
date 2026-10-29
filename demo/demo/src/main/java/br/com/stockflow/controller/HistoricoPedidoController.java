
package br.com.stockflow.controller;

import br.com.stockflow.dto.HistoricoPedidoRespostaDTO;
import br.com.stockflow.service.HistoricoPedidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos/{pedidoId}/historico")
public class HistoricoPedidoController {

    private final HistoricoPedidoService historicoPedidoService;

    public HistoricoPedidoController(
            HistoricoPedidoService historicoPedidoService) {
        this.historicoPedidoService = historicoPedidoService;
    }

    @GetMapping
    public List<HistoricoPedidoRespostaDTO> listarPorPedido(
            @PathVariable Long pedidoId) {
        return historicoPedidoService.listarPorPedido(pedidoId);
    }
}

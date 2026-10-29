package br.com.stockflow.service;

import br.com.stockflow.dto.HistoricoPedidoRespostaDTO;
import br.com.stockflow.entity.HistoricoPedido;
import br.com.stockflow.entity.Pedido;
import br.com.stockflow.mapper.HistoricoPedidoMapper;
import br.com.stockflow.repository.HistoricoPedidoRepository;
import br.com.stockflow.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoricoPedidoService {

    private final HistoricoPedidoRepository historicoPedidoRepository;
    private final PedidoRepository pedidoRepository;

    public HistoricoPedidoService(
            HistoricoPedidoRepository historicoPedidoRepository,
            PedidoRepository pedidoRepository) {
        this.historicoPedidoRepository = historicoPedidoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public void registrar(
            Pedido pedido,
            String tipoEvento,
            String descricao,
            String dadosAnteriores,
            String dadosNovos,
            String usuarioResponsavel) {

        HistoricoPedido historico = new HistoricoPedido(
                pedido,
                tipoEvento,
                descricao,
                dadosAnteriores,
                dadosNovos,
                usuarioResponsavel
        );

        historicoPedidoRepository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<HistoricoPedidoRespostaDTO> listarPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new EntityNotFoundException(
                    "Pedido não encontrado com o ID: " + pedidoId
            );
        }

        return historicoPedidoRepository
                .findByPedido_IdOrderByDataEventoDesc(pedidoId)
                .stream()
                .map(HistoricoPedidoMapper::paraResposta)
                .toList();
    }
}

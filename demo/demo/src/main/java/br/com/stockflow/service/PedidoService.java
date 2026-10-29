package br.com.stockflow.service;

import br.com.stockflow.dto.PedidoCadastroDTO;
import br.com.stockflow.dto.PedidoRespostaDTO;
import br.com.stockflow.entity.Cliente;
import br.com.stockflow.entity.Pedido;
import br.com.stockflow.entity.StatusPedido;
import br.com.stockflow.mapper.PedidoMapper;
import br.com.stockflow.repository.ClienteRepository;
import br.com.stockflow.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoPedidoService historicoPedidoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            HistoricoPedidoService historicoPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.historicoPedidoService = historicoPedidoService;
    }

    @Transactional
    public PedidoRespostaDTO criar(PedidoCadastroDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente não encontrado com o ID: " + dto.clienteId()
                ));

        if (!cliente.isAtivo()) {
            throw new IllegalStateException(
                    "Não é possível criar um pedido para um cliente inativo."
            );
        }

        Pedido pedido = new Pedido(cliente);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        historicoPedidoService.registrar(
                pedidoSalvo,
                "PEDIDO_CRIADO",
                "Pedido criado com sucesso.",
                null,
                "{\"status\":\"PENDENTE\",\"valorTotal\":0.00}",
                null
        );

        return PedidoMapper.paraResposta(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoRespostaDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(PedidoMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoRespostaDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado com o ID: " + id
                ));

        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional
    public PedidoRespostaDTO alterarStatus(
            Long pedidoId,
            StatusPedido novoStatus) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado com o ID: " + pedidoId
                ));

        if (novoStatus == null) {
            throw new IllegalArgumentException(
                    "O novo status do pedido é obrigatório."
            );
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO
                || pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new IllegalStateException(
                    "Não é possível alterar o status de um pedido cancelado ou concluído."
            );
        }

        StatusPedido statusAnterior = pedido.getStatus();

        if (statusAnterior == novoStatus) {
            throw new IllegalStateException(
                    "O pedido já possui esse status."
            );
        }

        pedido.setStatus(novoStatus);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        historicoPedidoService.registrar(
                pedidoSalvo,
                "STATUS_ALTERADO",
                "Status do pedido alterado de " + statusAnterior
                        + " para " + novoStatus + ".",
                "{\"status\":\"" + statusAnterior + "\"}",
                "{\"status\":\"" + novoStatus + "\"}",
                null
        );

        return PedidoMapper.paraResposta(pedidoSalvo);
    }
}

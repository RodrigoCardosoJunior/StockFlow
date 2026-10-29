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

        StatusPedido statusAnterior = pedido.getStatus();

        validarTransicaoStatus(statusAnterior, novoStatus);

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

    private void validarTransicaoStatus(
            StatusPedido statusAtual,
            StatusPedido novoStatus) {

        boolean transicaoPermitida = switch (statusAtual) {
            case PENDENTE ->
                    novoStatus == StatusPedido.CONFIRMADO
                            || novoStatus == StatusPedido.CANCELADO;

            case CONFIRMADO ->
                    novoStatus == StatusPedido.CONCLUIDO
                            || novoStatus == StatusPedido.CANCELADO;

            case CANCELADO, CONCLUIDO -> false;
        };

        if (!transicaoPermitida) {
            throw new IllegalStateException(
                    "Transição de status não permitida: "
                            + statusAtual + " para " + novoStatus + "."
            );
        }
    }
}

package br.com.stockflow.service;

import br.com.stockflow.dto.PedidoItemCadastroDTO;
import br.com.stockflow.mapper.PedidoItemMapper;
import br.com.stockflow.dto.PedidoItemRespostaDTO;
import br.com.stockflow.entity.Pedido;
import br.com.stockflow.entity.PedidoItem;
import br.com.stockflow.entity.Produto;
import br.com.stockflow.entity.StatusPedido;
import br.com.stockflow.repository.PedidoItemRepository;
import br.com.stockflow.repository.PedidoRepository;
import br.com.stockflow.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoItemService {

    private final PedidoItemRepository pedidoItemRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoItemService(
            PedidoItemRepository pedidoItemRepository,
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository) {
        this.pedidoItemRepository = pedidoItemRepository;
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public PedidoItemRespostaDTO adicionar(
            Long pedidoId,
            PedidoItemCadastroDTO dto) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado com o ID: " + pedidoId
                ));

        validarPedidoEditavel(pedido);

        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + dto.produtoId()
                ));

        if (!produto.isAtivo()) {
            throw new IllegalStateException(
                    "Não é possível adicionar um produto inativo."
            );
        }

        PedidoItem item = pedidoItemRepository
                .findByPedido_IdAndProduto_Id(pedidoId, dto.produtoId())
                .orElse(null);

        if (item != null) {
            item.setQuantidade(
                    Math.addExact(item.getQuantidade(), dto.quantidade())
            );
        } else {
            item = new PedidoItem(
                    pedido,
                    produto,
                    dto.quantidade(),
                    produto.getPreco()
            );
        }

        PedidoItem itemSalvo = pedidoItemRepository.save(item);

        recalcularTotal(pedido);

        return PedidoItemMapper.paraResposta(itemSalvo);
    }

    @Transactional
    public void remover(Long pedidoId, Long produtoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado com o ID: " + pedidoId
                ));

        validarPedidoEditavel(pedido);

        PedidoItem item = pedidoItemRepository
                .findByPedido_IdAndProduto_Id(pedidoId, produtoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Esse produto não está presente no pedido informado."
                ));

        pedidoItemRepository.delete(item);
        pedidoItemRepository.flush();

        recalcularTotal(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoItemRespostaDTO> listarPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new EntityNotFoundException(
                    "Pedido não encontrado com o ID: " + pedidoId
            );
        }

        return pedidoItemRepository.findByPedido_Id(pedidoId)
                .stream()
                .map(PedidoItemMapper::paraResposta)
                .toList();
    }

    private void validarPedidoEditavel(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CANCELADO
                || pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new IllegalStateException(
                    "Não é possível alterar um pedido cancelado ou concluído."
            );
        }
    }

    private void recalcularTotal(Pedido pedido) {
        List<PedidoItem> itens =
                pedidoItemRepository.findByPedido_Id(pedido.getId());

        BigDecimal total = itens.stream()
                .map(PedidoItem::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);
    }
}

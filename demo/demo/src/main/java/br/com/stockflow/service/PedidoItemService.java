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
    private final HistoricoPedidoService historicoPedidoService;

    public PedidoItemService(
            PedidoItemRepository pedidoItemRepository,
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            HistoricoPedidoService historicoPedidoService) {
        this.pedidoItemRepository = pedidoItemRepository;
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.historicoPedidoService = historicoPedidoService;
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

        // Guardamos os valores anteriores antes de alterar o pedido.
        BigDecimal totalAnterior = pedido.getValorTotal();

        PedidoItem item = pedidoItemRepository
                .findByPedido_IdAndProduto_Id(pedidoId, dto.produtoId())
                .orElse(null);

        boolean itemExistente = item != null;
        Integer quantidadeAnterior = itemExistente
                ? item.getQuantidade()
                : null;

        if (itemExistente) {
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

        // Atualiza o total antes de registrar o histórico.
        recalcularTotal(pedido);

        BigDecimal totalNovo = pedido.getValorTotal();

        String tipoEvento = itemExistente
                ? "QUANTIDADE_ALTERADA"
                : "ITEM_ADICIONADO";

        String descricao = itemExistente
                ? "Quantidade do produto " + produto.getNome()
                + " alterada de " + quantidadeAnterior
                + " para " + itemSalvo.getQuantidade() + "."
                : "Produto " + produto.getNome()
                + " adicionado ao pedido com quantidade "
                + itemSalvo.getQuantidade() + ".";

        String dadosAnteriores = itemExistente
                ? "{\"produtoId\":" + produto.getId()
                + ",\"quantidade\":" + quantidadeAnterior
                + ",\"valorTotal\":" + totalAnterior.toPlainString() + "}"
                : "{\"produtoId\":" + produto.getId()
                + ",\"quantidade\":0"
                + ",\"valorTotal\":" + totalAnterior.toPlainString() + "}";

        String dadosNovos =
                "{\"produtoId\":" + produto.getId()
                        + ",\"quantidade\":" + itemSalvo.getQuantidade()
                        + ",\"valorTotal\":" + totalNovo.toPlainString() + "}";

        historicoPedidoService.registrar(
                pedido,
                tipoEvento,
                descricao,
                dadosAnteriores,
                dadosNovos,
                null
        );

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

        // Captura os dados antes da remoção.
        Integer quantidadeRemovida = item.getQuantidade();
        BigDecimal precoUnitario = item.getPrecoUnitario();
        BigDecimal subtotalRemovido = item.calcularSubtotal();
        BigDecimal totalAnterior = pedido.getValorTotal();

        String nomeProduto = item.getProduto().getNome();

        pedidoItemRepository.delete(item);
        pedidoItemRepository.flush();

        // Recalcula o total sem o item removido.
        recalcularTotal(pedido);

        BigDecimal totalNovo = pedido.getValorTotal();

        String dadosAnteriores =
                "{\"produtoId\":" + produtoId
                        + ",\"quantidade\":" + quantidadeRemovida
                        + ",\"precoUnitario\":" + precoUnitario.toPlainString()
                        + ",\"subtotal\":" + subtotalRemovido.toPlainString()
                        + ",\"valorTotal\":" + totalAnterior.toPlainString() + "}";

        String dadosNovos =
                "{\"produtoId\":" + produtoId
                        + ",\"quantidade\":0"
                        + ",\"valorTotal\":" + totalNovo.toPlainString() + "}";

        historicoPedidoService.registrar(
                pedido,
                "ITEM_REMOVIDO",
                "Produto " + nomeProduto + " removido do pedido. "
                        + "Quantidade removida: " + quantidadeRemovida + ".",
                dadosAnteriores,
                dadosNovos,
                null
        );
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

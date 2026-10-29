package br.com.stockflow.mapper;

import br.com.stockflow.dto.PedidoItemRespostaDTO;
import br.com.stockflow.entity.PedidoItem;

public class PedidoItemMapper {

    private PedidoItemMapper() {
    }

    public static PedidoItemRespostaDTO paraResposta(PedidoItem item) {
        return new PedidoItemRespostaDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.calcularSubtotal()
        );
    }
}

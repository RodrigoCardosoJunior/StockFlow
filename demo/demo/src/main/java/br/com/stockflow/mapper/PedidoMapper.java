package br.com.stockflow.mapper;

import br.com.stockflow.dto.PedidoRespostaDTO;
import br.com.stockflow.entity.Pedido;

public class PedidoMapper {

    private PedidoMapper() {
    }

    public static PedidoRespostaDTO paraResposta(Pedido pedido) {
        return new PedidoRespostaDTO(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getDataCriacao(),
                pedido.getDataAtualizacao()
        );
    }
}

package br.com.stockflow.mapper;

import br.com.stockflow.dto.HistoricoPedidoRespostaDTO;
import br.com.stockflow.entity.HistoricoPedido;

public class HistoricoPedidoMapper {

    private HistoricoPedidoMapper() {
    }

    public static HistoricoPedidoRespostaDTO paraResposta(
            HistoricoPedido historico) {

        return new HistoricoPedidoRespostaDTO(
                historico.getId(),
                historico.getPedido().getId(),
                historico.getTipoEvento(),
                historico.getDescricao(),
                historico.getDadosAnteriores(),
                historico.getDadosNovos(),
                historico.getUsuarioResponsavel(),
                historico.getDataEvento()
        );
    }
}

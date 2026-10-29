package br.com.stockflow.dto;

import java.math.BigDecimal;

public record PedidoItemRespostaDTO(Long id,
                                    Long produtoId,
                                    String nomeProduto,
                                    Integer quantidade,
                                    BigDecimal precoUnitario,
                                    BigDecimal subtotal) {
}

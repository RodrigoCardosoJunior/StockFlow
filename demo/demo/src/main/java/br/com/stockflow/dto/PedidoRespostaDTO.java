package br.com.stockflow.dto;

import br.com.stockflow.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRespostaDTO(Long id,
                                Long clienteId,
                                StatusPedido status,
                                BigDecimal valorTotal,
                                LocalDateTime dataCriacao,
                                LocalDateTime dataAtualizacao) {
}

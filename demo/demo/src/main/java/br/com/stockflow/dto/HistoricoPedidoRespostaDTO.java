package br.com.stockflow.dto;

import java.time.LocalDateTime;

public record HistoricoPedidoRespostaDTO(Long id,
                                         Long pedidoId,
                                         String tipoEvento,
                                         String descricao,
                                         String dadosAnteriores,
                                         String dadosNovos,
                                         String usuarioResponsavel,
                                         LocalDateTime dataEvento) {}

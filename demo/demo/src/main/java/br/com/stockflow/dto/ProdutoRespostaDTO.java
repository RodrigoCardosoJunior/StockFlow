package br.com.stockflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoRespostaDTO(Long id,
                                 String nome,
                                 String descricao,
                                 BigDecimal preco,
                                 boolean ativo,
                                 LocalDateTime dataCriacao) {
}

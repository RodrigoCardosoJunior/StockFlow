package br.com.stockflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoItemCadastroDTO(

        @NotNull(message = "O ID do produto é obrigatório.")
        @Positive(message = "O ID do produto deve ser positivo.")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantidade

) {
}

package br.com.stockflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoCadastroDTO(

        @NotNull(message = "O ID do cliente é obrigatório.")
        @Positive(message = "O ID do cliente deve ser positivo.")
        Long clienteId

) {
}

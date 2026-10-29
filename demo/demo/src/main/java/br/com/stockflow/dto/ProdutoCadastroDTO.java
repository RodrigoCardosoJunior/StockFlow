package br.com.stockflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoCadastroDTO(

        @NotBlank(message = "O nome do produto é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        BigDecimal preco

) {
}

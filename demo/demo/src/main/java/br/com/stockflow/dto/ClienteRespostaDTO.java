package br.com.stockflow.dto;

import java.time.LocalDateTime;

/* Esse DTO será usado quando a API devolver os dados de um cliente */
public record ClienteRespostaDTO(Long id,
                                 String nome,
                                 String email,
                                 String telefone,
                                 boolean ativo,
                                 LocalDateTime dataCriacao) {

}

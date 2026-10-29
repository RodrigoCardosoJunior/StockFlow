package br.com.stockflow.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(
            EntityNotFoundException ex) {

        return criarResposta(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> tratarArgumentoInvalido(
            IllegalArgumentException ex) {

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Argumento inválido",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> tratarEstadoInvalido(
            IllegalStateException ex) {

        return criarResposta(
                HttpStatus.CONFLICT,
                "Operação não permitida",
                ex.getMessage()
        );
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> tratarDadosInvalidos(Exception ex) {

        String mensagem = "Os dados enviados são inválidos.";

        if (ex instanceof MethodArgumentNotValidException erroValidacao
                && erroValidacao.getBindingResult().getFieldError() != null) {
            mensagem = erroValidacao.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                mensagem
        );
    }

    private ResponseEntity<ApiError> criarResposta(
            HttpStatus status,
            String erro,
            String mensagem) {

        ApiError resposta = new ApiError(
                LocalDateTime.now(),
                status.value(),
                erro,
                mensagem
        );

        return ResponseEntity.status(status).body(resposta);
    }
}

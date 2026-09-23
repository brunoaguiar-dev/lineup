package com.lineup.error;

import com.lineup.escola.EscolaNaoEncontrada;
import com.lineup.usuario.CredenciaisInvalidas;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estender ResponseEntityExceptionHandler faz o handler padrão do Boot sair de
 * cena, porque a autoconfiguração dele tem @ConditionalOnMissingBean nessa
 * classe. O que não for sobrescrito aqui continua vindo dela.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EscolaNaoEncontrada.class)
    ProblemDetail escolaNaoEncontrada(EscolaNaoEncontrada e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidas.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidas e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = createProblemDetail(e, status, "Campos inválidos", null, null, request);
        problema.setProperty("campos", campos);
        return ResponseEntity.status(status).body(problema);
    }
}

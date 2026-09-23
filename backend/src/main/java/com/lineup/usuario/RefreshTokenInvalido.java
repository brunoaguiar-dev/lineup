package com.lineup.usuario;

// Mesma mensagem para token inexistente, vencido e reusado.
public class RefreshTokenInvalido extends RuntimeException {

    RefreshTokenInvalido() {
        super("Sessão expirada. Faça login novamente.");
    }
}

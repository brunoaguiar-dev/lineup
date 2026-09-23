package com.lineup.autenticacao;

public class CredenciaisInvalidas extends RuntimeException {

    CredenciaisInvalidas() {
        super("Email ou senha inválidos");
    }
}

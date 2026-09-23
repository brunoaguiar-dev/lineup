package com.lineup.usuario;

public class CredenciaisInvalidas extends RuntimeException {

    CredenciaisInvalidas() {
        super("Email ou senha inválidos");
    }
}

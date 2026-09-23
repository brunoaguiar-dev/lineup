package com.lineup.escola;

import java.util.UUID;

public class EscolaNaoEncontrada extends RuntimeException {

    EscolaNaoEncontrada(UUID id) {
        super("Escola não encontrada: " + id);
    }
}

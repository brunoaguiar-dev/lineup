package com.lineup.autenticacao;

import java.time.Duration;

public class MuitasTentativas extends RuntimeException {

    MuitasTentativas(Duration janela) {
        super("Muitas tentativas de login. Tente novamente em " + janela.toMinutes() + " minutos.");
    }
}

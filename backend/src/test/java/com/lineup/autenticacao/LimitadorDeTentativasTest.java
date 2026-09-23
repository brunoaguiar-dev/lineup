package com.lineup.autenticacao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimitadorDeTentativasTest {

    private static final String IP = "200.0.0.1";
    private static final String EMAIL = "bruno@lineup.com";

    private final LimitadorDeTentativas limitador = new LimitadorDeTentativas();

    @Test
    void liberaEnquantoAsFalhasEstaoAbaixoDoLimite() {
        registrarFalhas(9);

        assertThatCode(() -> limitador.verificar(IP, EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void barraQuandoAsFalhasChegamAoLimite() {
        registrarFalhas(10);

        assertThatThrownBy(() -> limitador.verificar(IP, EMAIL))
                .isInstanceOf(MuitasTentativas.class);
    }

    @Test
    void acertarZeraOContador() {
        registrarFalhas(10);
        limitador.registrarAcerto(IP, EMAIL);

        assertThatCode(() -> limitador.verificar(IP, EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void oMesmoEmailDeOutroIpContinuaContando() {
        registrarFalhas(10);

        assertThatThrownBy(() -> limitador.verificar("200.0.0.2", EMAIL))
                .isInstanceOf(MuitasTentativas.class);
    }

    @Test
    void oMesmoIpComOutroEmailContinuaContando() {
        registrarFalhas(10);

        assertThatThrownBy(() -> limitador.verificar(IP, "outro@lineup.com"))
                .isInstanceOf(MuitasTentativas.class);
    }

    private void registrarFalhas(int quantas) {
        for (int i = 0; i < quantas; i++) {
            limitador.registrarFalha(IP, EMAIL);
        }
    }
}

package com.lineup.autenticacao;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Component
class LimitadorDeTentativas {

    static final Duration JANELA = Duration.ofMinutes(5);
    private static final int LIMITE = 10;

    // Duas chaves por tentativa
    private final Cache<String, AtomicInteger> falhas = Caffeine.newBuilder()
            .expireAfterWrite(JANELA)
            .maximumSize(10_000)
            .build();

    void verificar(String ip, String email) {
        if (contagem(chaveIp(ip)) >= LIMITE || contagem(chaveEmail(email)) >= LIMITE) {
            throw new MuitasTentativas(JANELA);
        }
    }

    void registrarFalha(String ip, String email) {
        incrementar(chaveIp(ip));
        incrementar(chaveEmail(email));
    }

    void registrarAcerto(String ip, String email) {
        falhas.invalidate(chaveIp(ip));
        falhas.invalidate(chaveEmail(email));
    }

    private int contagem(String chave) {
        AtomicInteger contador = falhas.getIfPresent(chave);
        return contador == null ? 0 : contador.get();
    }

    // Incrementar não escreve no cache, então a janela conta da primeira falha
    // e não se estende a cada falha nova.
    private void incrementar(String chave) {
        falhas.get(chave, nova -> new AtomicInteger()).incrementAndGet();
    }

    private String chaveIp(String ip) {
        return "ip:" + ip;
    }

    private String chaveEmail(String email) {
        return "email:" + email.toLowerCase(Locale.ROOT);
    }
}

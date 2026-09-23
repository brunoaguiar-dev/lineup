package com.lineup.usuario;

import com.lineup.config.TokenProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
// A revogação por reuso acontece logo antes da exceção, e sem isso ela
// voltaria atrás junto com o resto da transação.
@Transactional(noRollbackFor = RefreshTokenInvalido.class)
class RefreshTokenService {

    private static final int BYTES_DO_TOKEN = 32;

    private final SecureRandom aleatorio = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final TokenProperties propriedades;

    RefreshTokenService(RefreshTokenRepository repository, TokenProperties propriedades) {
        this.repository = repository;
        this.propriedades = propriedades;
    }

    String emitir(Usuario usuario, UUID familia) {
        String valor = novoValor();

        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setFamilia(familia);
        token.setTokenHash(hash(valor));
        token.setExpiraEm(Instant.now().plus(propriedades.validadeDoRefreshToken()));
        repository.save(token);

        return valor;
    }

    RefreshToken consumir(String valor) {
        Instant agora = Instant.now();
        RefreshToken token = repository.findByTokenHash(hash(valor))
                .orElseThrow(RefreshTokenInvalido::new);

        if (token.jaSaiuDeCirculacao()) {
            repository.revogarFamilia(token.getFamilia(), agora);
            throw new RefreshTokenInvalido();
        }

        if (token.vencido(agora)) {
            throw new RefreshTokenInvalido();
        }

        token.setUsadoEm(agora);
        return token;
    }

    void revogarSessao(String valor) {
        repository.findByTokenHash(hash(valor))
                .ifPresent(token -> repository.revogarFamilia(token.getFamilia(), Instant.now()));
    }

    private String novoValor() {
        byte[] bytes = new byte[BYTES_DO_TOKEN];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}

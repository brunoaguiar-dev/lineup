package com.lineup.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O Spring Security 7 acrescenta FACTOR_BEARER sozinho, para registrar que a
 * autenticação veio de um token. Por isso as asserções olham só os ROLE_.
 */
class ConversorDePapelTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void aClaimPapelViraUmPapelDoSpringSecurity() {
        var autenticacao = config.jwtAuthenticationConverter().convert(tokenComPapel("ADMIN_ESCOLA"));

        assertThat(autenticacao.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN_ESCOLA");
    }

    @Test
    void tokenSemPapelNaoRecebeNenhum() {
        var autenticacao = config.jwtAuthenticationConverter().convert(tokenSemPapel());

        assertThat(autenticacao.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .noneMatch(papel -> papel.startsWith("ROLE_"));
    }

    private Jwt tokenComPapel(String papel) {
        return jwt().claim("papel", papel).build();
    }

    private Jwt tokenSemPapel() {
        return jwt().build();
    }

    private Jwt.Builder jwt() {
        return Jwt.withTokenValue("token-de-teste")
                .header("alg", "HS256")
                .subject("6c1c1e3a-0b6a-4a6e-9f47-9f2a1c1e0b6a")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900));
    }
}

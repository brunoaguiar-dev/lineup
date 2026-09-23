package com.lineup.autenticacao;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AcessoTest {

    private static final UUID ESCOLA = UUID.fromString("0f1e2d3c-4b5a-6978-8796-a5b4c3d2e1f0");
    private static final UUID OUTRA_ESCOLA = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private final Acesso acesso = new Acesso();

    @Test
    void superAdminEntraEmQualquerEscola() {
        assertThat(acesso.naEscola(ESCOLA, comPapel("SUPER_ADMIN", null))).isTrue();
        assertThat(acesso.naEscola(OUTRA_ESCOLA, comPapel("SUPER_ADMIN", null))).isTrue();
    }

    @Test
    void adminEntraNaPropriaEscola() {
        assertThat(acesso.naEscola(ESCOLA, comPapel("ADMIN_ESCOLA", ESCOLA))).isTrue();
    }

    @Test
    void adminNaoEntraNaEscolaDeOutro() {
        assertThat(acesso.naEscola(OUTRA_ESCOLA, comPapel("ADMIN_ESCOLA", ESCOLA))).isFalse();
    }

    @Test
    void semAutenticacaoNaoEntra() {
        assertThat(acesso.naEscola(ESCOLA, null)).isFalse();
    }

    @Test
    void autenticacaoQueNaoVeioDeTokenNaoEntra() {
        Authentication outraForma = new UsernamePasswordAuthenticationToken(
                "alguem", "senha", List.of(new SimpleGrantedAuthority("ROLE_ADMIN_ESCOLA")));

        assertThat(acesso.naEscola(ESCOLA, outraForma)).isFalse();
    }

    private Authentication comPapel(String papel, UUID escolaId) {
        Jwt.Builder token = Jwt.withTokenValue("token-de-teste")
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .claim("papel", papel);

        if (escolaId != null) {
            token.claim("escolaId", escolaId.toString());
        }

        return new JwtAuthenticationToken(token.build(),
                List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
    }
}

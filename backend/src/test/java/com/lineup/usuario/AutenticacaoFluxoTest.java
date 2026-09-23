package com.lineup.usuario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineup.config.ApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AutenticacaoFluxoTest {

    private static final String EMAIL = "bruno@lineup.com";
    private static final String SENHA = "uma-senha-qualquer";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void cadastraOSuperAdmin() {
        if (usuarios.findByEmailIgnoreCase(EMAIL).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setEmail(EMAIL);
            usuario.setSenhaHash(passwordEncoder.encode(SENHA));
            usuario.setPapel(Papel.SUPER_ADMIN);
            usuarios.save(usuario);
        }
    }

    @Test
    void oAccessTokenEmitidoAbreAsRotasProtegidas() throws Exception {
        String rota = "/api/v1/escolas/" + UUID.randomUUID();

        mvc.perform(get(rota)).andExpect(status().isUnauthorized());

        // 404 e não 401: a escola não existe, mas o token foi aceito.
        mvc.perform(get(rota).header("Authorization", "Bearer " + logar().get("accessToken").asText()))
                .andExpect(status().isNotFound());
    }

    @Test
    void renovarTrocaOsDoisTokens() throws Exception {
        JsonNode login = logar();
        JsonNode renovado = renovar(login.get("refreshToken").asText(), status().isOk());

        assertThat(renovado.get("refreshToken").asText())
                .isNotEqualTo(login.get("refreshToken").asText());
        assertThat(renovado.get("accessToken").asText()).isNotBlank();
    }

    @Test
    void reapresentarOTokenAntigoDerrubaASessaoInteira() throws Exception {
        JsonNode login = logar();
        String antigo = login.get("refreshToken").asText();

        JsonNode renovado = renovar(antigo, status().isOk());
        String atual = renovado.get("refreshToken").asText();

        renovar(antigo, status().isUnauthorized());

        renovar(atual, status().isUnauthorized());
    }

    @Test
    void depoisDoLogoutORefreshNaoVale() throws Exception {
        JsonNode login = logar();
        String refresh = login.get("refreshToken").asText();

        mvc.perform(post(ApiPaths.AUTH + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoComRefresh(refresh)))
                .andExpect(status().isNoContent());

        renovar(refresh, status().isUnauthorized());
    }

    private JsonNode logar() throws Exception {
        String corpo = mvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(EMAIL, SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private JsonNode renovar(String refreshToken, org.springframework.test.web.servlet.ResultMatcher esperado)
            throws Exception {
        String corpo = mvc.perform(post(ApiPaths.AUTH + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoComRefresh(refreshToken)))
                .andExpect(esperado)
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private String corpoComRefresh(String refreshToken) {
        return """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
    }
}

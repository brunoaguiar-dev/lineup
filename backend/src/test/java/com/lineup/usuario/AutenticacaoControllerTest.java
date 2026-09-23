package com.lineup.usuario;

import com.lineup.config.ApiPaths;
import com.lineup.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacaoController.class)
@Import(SecurityConfig.class)
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AutenticacaoService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void loginValidoDevolveOToken() throws Exception {
        Instant expiraEm = Instant.parse("2026-09-20T19:30:00Z");
        given(service.logar(any(), any())).willReturn(new TokenResponse("um.token.qualquer", expiraEm, "um.refresh.qualquer"));

        mvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@lineup.com","senha":"uma-senha"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("um.token.qualquer"));
    }

    @Test
    void senhaErradaDevolve401SemDizerOQueEstaErrado() throws Exception {
        willThrow(new CredenciaisInvalidas()).given(service).logar(any(), any());

        mvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@lineup.com","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Email ou senha inválidos"));
    }

    @Test
    void limiteEstouradoDevolve429() throws Exception {
        willThrow(new MuitasTentativas(LimitadorDeTentativas.JANELA)).given(service).logar(any(), any());

        mvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@lineup.com","senha":"errada"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Muitas tentativas de login. Tente novamente em 5 minutos."));
    }

    @Test
    void refreshInvalidoDevolve401() throws Exception {
        willThrow(new RefreshTokenInvalido()).given(service).renovar(any());

        mvc.perform(post(ApiPaths.AUTH + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"qualquer-coisa"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Sessão expirada. Faça login novamente."));
    }

    @Test
    void logoutDevolve204() throws Exception {
        mvc.perform(post(ApiPaths.AUTH + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"qualquer-coisa"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void emailMalFormadoDevolve400() throws Exception {
        mvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nao-e-email","senha":"uma-senha"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }
}

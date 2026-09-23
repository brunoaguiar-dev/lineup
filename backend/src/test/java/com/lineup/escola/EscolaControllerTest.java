package com.lineup.escola;

import com.lineup.autenticacao.Acesso;
import com.lineup.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EscolaController.class)
@Import({SecurityConfig.class, Acesso.class})
@WithMockUser(roles = "SUPER_ADMIN")
class EscolaControllerTest {

    private static final UUID ID = UUID.fromString("99b554d6-8af2-4356-a550-a4e7481b9c26");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EscolaService service;

    // Exigido pelo resource server. Quem autentica aqui é o @WithMockUser.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void criarDevolve201ComLocation() throws Exception {
        given(service.criar(any())).willReturn(umaResposta());

        mvc.perform(post(EscolaController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Surf Leste Oeste","uf":"CE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", locationEsperado()))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void buscarInexistenteDevolve404EmProblemDetail() throws Exception {
        willThrow(new EscolaNaoEncontrada(ID)).given(service).buscarPorId(ID);

        mvc.perform(get(EscolaController.BASE_PATH + "/" + ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Escola não encontrada: " + ID));
    }

    @Test
    void corpoInvalidoDevolve400ListandoOsCampos() throws Exception {
        mvc.perform(post(EscolaController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","uf":"CEE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.uf").exists());
    }

    @Test
    @WithAnonymousUser
    void semCredencialDevolve401() throws Exception {
        mvc.perform(get(EscolaController.BASE_PATH + "/" + ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDeEscolaNaoCriaEscola() throws Exception {
        mvc.perform(post(EscolaController.BASE_PATH)
                        .with(comoAdminDa(ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Surf Leste Oeste","uf":"CE"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeEscolaVeAPropriaEscola() throws Exception {
        given(service.buscarPorId(ID)).willReturn(umaResposta());

        mvc.perform(get(EscolaController.BASE_PATH + "/" + ID).with(comoAdminDa(ID)))
                .andExpect(status().isOk());
    }

    @Test
    void adminDeEscolaNaoVeEscolaDeOutro() throws Exception {
        mvc.perform(get(EscolaController.BASE_PATH + "/" + ID)
                        .with(comoAdminDa(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor comoAdminDa(UUID escolaId) {
        return jwt()
                .jwt(token -> token.claim("papel", "ADMIN_ESCOLA").claim("escolaId", escolaId.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_ESCOLA"));
    }

    private String locationEsperado() {
        return "http://localhost" + EscolaController.BASE_PATH + "/" + ID;
    }

    private EscolaResponse umaResposta() {
        Instant agora = Instant.parse("2026-09-20T19:15:32Z");
        return new EscolaResponse(ID, "Surf Leste Oeste", null, null, null,
                null, null, "CE", agora, agora);
    }
}

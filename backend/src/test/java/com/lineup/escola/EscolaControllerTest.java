package com.lineup.escola;

import com.lineup.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre o contrato HTTP: status, header e formato de erro. A persistência tem
 * teste próprio, então aqui o service é dublado.
 */
@WebMvcTest(EscolaController.class)
@Import(SecurityConfig.class)
@WithMockUser
class EscolaControllerTest {

    private static final UUID ID = UUID.fromString("99b554d6-8af2-4356-a550-a4e7481b9c26");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EscolaService service;

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

    private String locationEsperado() {
        return "http://localhost" + EscolaController.BASE_PATH + "/" + ID;
    }

    private EscolaResponse umaResposta() {
        Instant agora = Instant.parse("2026-09-20T19:15:32Z");
        return new EscolaResponse(ID, "Surf Leste Oeste", null, null, null,
                null, null, "CE", agora, agora);
    }
}

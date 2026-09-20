package com.lineup.escola;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda contra Postgres de verdade porque o que está a ser testado só existe lá:
 * o ‘trigger’ da V3 e os ‘defaults’ das colunas de auditoria.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class EscolaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private EscolaRepository repository;

    @Test
    void gravaEDeixaOBancoPreencherAsColunasDeAuditoria() {
        Escola salva = repository.saveAndFlush(novaEscola());

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getCriadoEm()).isNotNull();
        assertThat(salva.getAtualizadoEm()).isNotNull();
        assertThat(salva.getUf()).isEqualTo("CE");
    }

    /**
     * Sem NOT_SUPPORTED o insert e o ‘update’ rodariam na transação do teste, e o
     * now() do Postgres devolveria o mesmo horário para os dois. Suspendendo a
     * transação, cada chamada ao repository abre a sua, e os instantes diferem.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void triggerMoveOAtualizadoEmSemTocarNoCriadoEm() {
        Escola salva = repository.saveAndFlush(novaEscola());
        Instant criadoEm = salva.getCriadoEm();
        Instant atualizadoEmInicial = salva.getAtualizadoEm();

        salva.setNome("Surf Leste Oeste Fortaleza");
        Escola atualizada = repository.saveAndFlush(salva);

        assertThat(atualizada.getAtualizadoEm()).isAfter(atualizadoEmInicial);
        assertThat(atualizada.getCriadoEm()).isEqualTo(criadoEm);
    }

    private Escola novaEscola() {
        Escola escola = new Escola();
        escola.setNome("Surf Leste Oeste");
        escola.setPraia("Leste Oeste");
        escola.setCidade("Fortaleza");
        escola.setUf("CE");
        return escola;
    }
}

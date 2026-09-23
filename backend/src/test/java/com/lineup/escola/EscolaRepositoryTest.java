package com.lineup.escola;

import com.lineup.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda contra Postgres de verdade: o que se testa aqui é o mapeamento da
 * entidade contra o schema, que banco em memória não reproduz.
 */
@DataJpaTest
// O slice não carrega as @Configuration da aplicação, e sem o auditing o
// listener do Spring Data não reclama: ele só não preenche as datas.
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class EscolaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private EscolaRepository repository;

    @Test
    void gravaEPreencheAsColunasDeAuditoria() {
        Escola salva = repository.save(novaEscola());

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getCriadoEm()).isNotNull();
        assertThat(salva.getAtualizadoEm()).isNotNull();
        assertThat(salva.getUf()).isEqualTo("CE");
    }

    @Test
    void atualizacaoMoveOAtualizadoEmSemTocarNoCriadoEm() {
        Escola salva = repository.save(novaEscola());
        Instant criadoEm = salva.getCriadoEm();
        Instant atualizadoEmInicial = salva.getAtualizadoEm();

        salva.setNome("Surf Leste Oeste Fortaleza");
        repository.flush();

        assertThat(salva.getAtualizadoEm()).isAfter(atualizadoEmInicial);
        assertThat(salva.getCriadoEm()).isEqualTo(criadoEm);
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

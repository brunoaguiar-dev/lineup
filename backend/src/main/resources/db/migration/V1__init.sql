-- LineUp -- V1: schema inicial
--
-- Convencoes:
--   PK em UUID; toda tabela carrega escola_id (multi-tenant);
--   enums como VARCHAR + CHECK (ENUM nativo do Postgres e caro de evoluir);
--   criado_em / atualizado_em em todas as tabelas; nomes no singular, snake_case.

CREATE TABLE escola (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome          VARCHAR(150) NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(150),
    endereco      VARCHAR(255),
    praia         VARCHAR(100),
    cidade        VARCHAR(100),
    uf            CHAR(2),
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Toda regra de negocio le seus limites daqui; nenhum numero fixo no codigo.
CREATE TABLE configuracao_escola (
    id                        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id                 UUID        NOT NULL UNIQUE REFERENCES escola(id) ON DELETE CASCADE,
    capacidade_padrao_aula    INT         NOT NULL DEFAULT 3  CHECK (capacidade_padrao_aula > 0),
    -- Cancelar com esta antecedencia devolve a aula ao saldo; abaixo dela e furo.
    horas_limite_cancelamento INT         NOT NULL DEFAULT 2  CHECK (horas_limite_cancelamento >= 0),
    -- Furo = cancelou tarde ou nao apareceu. Os primeiros N do periodo sao perdoados.
    furos_tolerados_mes       INT         NOT NULL DEFAULT 1  CHECK (furos_tolerados_mes >= 0),
    tolerancia_atraso_minutos INT         NOT NULL DEFAULT 15 CHECK (tolerancia_atraso_minutos >= 0),
    aulas_liberadas_em_atraso INT         NOT NULL DEFAULT 1  CHECK (aulas_liberadas_em_atraso >= 0),
    duracao_periodo_dias      INT         NOT NULL DEFAULT 30 CHECK (duracao_periodo_dias > 0),
    criado_em                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE instrutor (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID         NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    nome          VARCHAR(150) NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(150),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE aluno (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id            UUID         NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    nome                 VARCHAR(150) NOT NULL,
    -- Opcional: crianca nao tem telefone proprio, o contato e o do responsavel.
    telefone             VARCHAR(20),
    email                VARCHAR(150),
    data_nascimento      DATE,

    -- NULL nos dois: o admin cadastra o basico e o aluno completa no primeiro acesso --
    nivel                VARCHAR(20)  CHECK (nivel IN ('INICIANTE','INTERMEDIARIO','AVANCADO')),
    sabe_nadar           BOOLEAN,
    tem_responsavel      BOOLEAN      NOT NULL DEFAULT FALSE,
    responsavel_nome     VARCHAR(150),
    responsavel_telefone VARCHAR(20),

    -- Lesao, alergia, medo de agua funda: o que o professor precisa saber na areia.
    observacoes          TEXT,
    ativo                BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT ck_aluno_responsavel CHECK (
        tem_responsavel = FALSE
        OR (responsavel_nome IS NOT NULL AND responsavel_telefone IS NOT NULL)
    ),
    CONSTRAINT ck_aluno_contato CHECK (
        tem_responsavel = TRUE
        OR telefone IS NOT NULL
    )
);

CREATE TABLE plano (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID          NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    nome          VARCHAR(100)  NOT NULL,
    aulas_por_mes INT           NOT NULL CHECK (aulas_por_mes > 0),
    preco         NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    ativo         BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_plano_nome UNIQUE (escola_id, nome)
);

-- O periodo e ancorado na data de pagamento, e nao no mes do calendario. --
CREATE TABLE matricula (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id          UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    aluno_id           UUID        NOT NULL REFERENCES aluno(id) ON DELETE CASCADE,
    plano_id           UUID        NOT NULL REFERENCES plano(id),
    data_matricula     DATE        NOT NULL,
    periodo_inicio     DATE        NOT NULL,
    periodo_fim        DATE        NOT NULL,
    pagamento_pendente BOOLEAN     NOT NULL DEFAULT FALSE,
    status             VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO'
                       CHECK (status IN ('AGUARDANDO_PAGAMENTO','ATIVA','INATIVA','CANCELADA')),
    criado_em          TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_matricula_periodo CHECK (periodo_fim > periodo_inicio)
);

CREATE UNIQUE INDEX uq_matricula_ativa_por_aluno
    ON matricula (aluno_id)
    WHERE status = 'ATIVA';

-- 1 credito = 1 aula do saldo do periodo. O que sobra expira na virada:
-- nao acumula para o periodo seguinte.
CREATE TABLE credito (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    matricula_id  UUID        NOT NULL REFERENCES matricula(id) ON DELETE CASCADE,
    origem        VARCHAR(30) NOT NULL
                  CHECK (origem IN ('MATRICULA','DEVOLUCAO_CANCELAMENTO','PERDAO_ADMIN','AJUSTE_ADMIN')),
    validade      DATE        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL'
                  CHECK (status IN ('DISPONIVEL','CONSUMIDO','EXPIRADO')),
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Molde recorrente da grade. Cada ocorrencia vira uma linha em aula.
CREATE TABLE horario_semanal (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    instrutor_id  UUID        REFERENCES instrutor(id) ON DELETE SET NULL,
    -- ISO-8601: 1 = segunda ... 7 = domingo
    dia_semana    SMALLINT    NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora          TIME        NOT NULL,
    capacidade    INT         NOT NULL DEFAULT 3 CHECK (capacidade > 0),
    modalidade    VARCHAR(30) NOT NULL DEFAULT 'SURF',
    ativo         BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Ocorrencia concreta. horario_id nulo = aula avulsa, criada fora da grade.
CREATE TABLE aula (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    horario_id    UUID        REFERENCES horario_semanal(id) ON DELETE SET NULL,
    instrutor_id  UUID        REFERENCES instrutor(id) ON DELETE SET NULL,
    data          DATE        NOT NULL,
    hora          TIME        NOT NULL,
    capacidade    INT         NOT NULL CHECK (capacidade > 0),
    modalidade    VARCHAR(30) NOT NULL DEFAULT 'SURF',
    status        VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
                  CHECK (status IN ('NORMAL','BLOQUEADA','CANCELADA','EXCEPCIONAL')),
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_aula_horario_data
    ON aula (horario_id, data)
    WHERE horario_id IS NOT NULL;

CREATE TABLE reserva (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id     UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    aula_id       UUID        NOT NULL REFERENCES aula(id) ON DELETE CASCADE,
    aluno_id      UUID        NOT NULL REFERENCES aluno(id) ON DELETE CASCADE,
    credito_id    UUID        REFERENCES credito(id),
    origem        VARCHAR(20) NOT NULL DEFAULT 'ALUNO'
                  CHECK (origem IN ('ALUNO','ADMIN_MANUAL')),
    status        VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA'
                  CHECK (status IN ('CONFIRMADA','CANCELADA')),
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_reserva_ativa_por_aluno
    ON reserva (aula_id, aluno_id)
    WHERE status = 'CONFIRMADA';

CREATE UNIQUE INDEX uq_reserva_ativa_por_credito
    ON reserva (credito_id)
    WHERE status = 'CONFIRMADA' AND credito_id IS NOT NULL;

CREATE TABLE presenca (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id      UUID        NOT NULL REFERENCES escola(id) ON DELETE CASCADE,
    reserva_id     UUID        NOT NULL UNIQUE REFERENCES reserva(id) ON DELETE CASCADE,
    situacao       VARCHAR(20) NOT NULL CHECK (situacao IN ('PRESENTE','FALTA')),
    registrado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_aula_escola_data     ON aula (escola_id, data);
CREATE INDEX idx_reserva_aula         ON reserva (aula_id);
CREATE INDEX idx_reserva_aluno        ON reserva (aluno_id);
CREATE INDEX idx_credito_disponivel   ON credito (matricula_id, status);
CREATE INDEX idx_matricula_aluno      ON matricula (aluno_id);
CREATE INDEX idx_horario_semanal_grade ON horario_semanal (escola_id, dia_semana) WHERE ativo;

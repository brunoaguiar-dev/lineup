-- Quem loga. O aluno que entra na água é a tabela aluno; aqui fica a
-- credencial, que para criança é a do responsável.

CREATE TABLE usuario (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Nulo só para SUPER_ADMIN, que opera a plataforma e não pertence à escola.
    escola_id     UUID         REFERENCES escola(id) ON DELETE CASCADE,
    email         VARCHAR(150) NOT NULL,
    -- Nulo até o usuário abrir o link de ativação e definir a senha. Folgado
    -- porque o tamanho depende do algoritmo: bcrypt gera 60, argon2 passa de 100.
    senha_hash    VARCHAR(255),
    papel         VARCHAR(20)  NOT NULL
                  CHECK (papel IN ('SUPER_ADMIN','ADMIN_ESCOLA','ALUNO')),
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_escola CHECK (
        (papel = 'SUPER_ADMIN' AND escola_id IS NULL)
        OR (papel <> 'SUPER_ADMIN' AND escola_id IS NOT NULL)
    )
);

-- O login é por email, então ele identifica a pessoa no sistema inteiro.
-- Em lower() para Maria@x.com e maria@x.com não virarem duas contas.
CREATE UNIQUE INDEX uq_usuario_email ON usuario (lower(email));

-- Serve para ativar a conta e, depois, para redefinir senha.
CREATE TABLE token_ativacao (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    -- SHA-256 do token, nunca o valor que vai no link: quem lê o banco
    -- conseguiria usar o link para assumir a conta.
    token_hash    CHAR(64)     NOT NULL,
    expira_em     TIMESTAMPTZ  NOT NULL,
    usado_em      TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_token_ativacao_hash ON token_ativacao (token_hash);
CREATE INDEX idx_token_ativacao_usuario ON token_ativacao (usuario_id);

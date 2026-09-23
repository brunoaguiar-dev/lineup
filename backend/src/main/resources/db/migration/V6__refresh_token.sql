-- Uma família por login. É por ela que se derruba a sessão inteira de uma vez.

CREATE TABLE refresh_token (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    familia       UUID         NOT NULL,
    -- SHA-256 do token, nunca o valor que vai para o cliente.
    token_hash    VARCHAR(64)  NOT NULL,
    expira_em     TIMESTAMPTZ  NOT NULL,
    usado_em      TIMESTAMPTZ,
    revogado_em   TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_refresh_token_hash ON refresh_token (token_hash);
CREATE INDEX idx_refresh_token_familia ON refresh_token (familia);

-- Mesma correção da V2 em escola.uf: o Hibernate recusa CHAR(n) mapeado para String.
ALTER TABLE token_ativacao ALTER COLUMN token_hash TYPE VARCHAR(64);

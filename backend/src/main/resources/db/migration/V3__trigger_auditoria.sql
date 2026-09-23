-- atualizado_em recebia now() na criação e nunca mais mudava. A regra fica no
-- banco para valer também em alteração feita na mão, por script ou por outro
-- cliente.

CREATE FUNCTION atualiza_auditoria() RETURNS trigger AS $$
BEGIN
    NEW.atualizado_em = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON escola
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON configuracao_escola
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON instrutor
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON aluno
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON plano
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON matricula
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON credito
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON horario_semanal
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON aula
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON reserva
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

CREATE TRIGGER trg_auditoria BEFORE UPDATE ON presenca
    FOR EACH ROW EXECUTE FUNCTION atualiza_auditoria();

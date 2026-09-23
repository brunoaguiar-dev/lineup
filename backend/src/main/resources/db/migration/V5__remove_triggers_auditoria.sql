-- As colunas de auditoria passam a ser preenchidas pela aplicação, com a
-- auditoria do Spring Data. A trigger dava uma garantia que este sistema não
-- precisa: existe uma aplicação só escrevendo neste banco. Em troca ela exigia
-- @Generated nas entidades e cuidado com flush a cada escrita.
--
-- O DEFAULT now() das colunas fica, como rede para insert feito fora da API.

DROP TRIGGER trg_auditoria ON escola;
DROP TRIGGER trg_auditoria ON configuracao_escola;
DROP TRIGGER trg_auditoria ON instrutor;
DROP TRIGGER trg_auditoria ON aluno;
DROP TRIGGER trg_auditoria ON plano;
DROP TRIGGER trg_auditoria ON matricula;
DROP TRIGGER trg_auditoria ON credito;
DROP TRIGGER trg_auditoria ON horario_semanal;
DROP TRIGGER trg_auditoria ON aula;
DROP TRIGGER trg_auditoria ON reserva;
DROP TRIGGER trg_auditoria ON presenca;

DROP FUNCTION atualiza_auditoria();

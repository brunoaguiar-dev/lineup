-- char(n) é preenchido com espaços até o tamanho fixo e a documentação do
-- Postgres recomenda varchar no lugar. Além disso, o Hibernate recusa validar
-- String contra bpchar.

ALTER TABLE escola ALTER COLUMN uf TYPE VARCHAR(2);

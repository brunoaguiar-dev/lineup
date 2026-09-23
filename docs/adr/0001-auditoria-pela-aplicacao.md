# 1. Auditoria pela aplicação, não por trigger

Data: 2026-09-20

Comecei com uma trigger no Postgres preenchendo `criado_em` e `atualizado_em`. O
argumento era que assim as colunas valem para qualquer escrita, inclusive um
`UPDATE` feito na mão. Isso importa quando várias aplicações escrevem no mesmo
schema. Aqui é uma só.

O custo apareceu no mapeamento. Precisei de `@Generated` nas entidades para o
Hibernate não sobrescrever o que o banco gerou, e de flush no serviço para o
valor novo chegar na resposta. Lendo o SQL de um teste descobri que
`@Generated(INSERT)` não impede a coluna de entrar no `UPDATE`: o `criado_em`
estava sendo reescrito a cada alteração. Só parou com `updatable = false`.

Ou seja, três peças de configuração para sustentar uma garantia que este sistema
não usa.

Troquei pela auditoria do Spring Data, que é `@CreatedDate` e
`@LastModifiedDate` numa `@MappedSuperclass` com `AuditingEntityListener`. A V5
derruba as onze triggers e a função que a V3 tinha criado. O `DEFAULT now()`
continua nas colunas, para insert feito fora da aplicação.

O que isso custa:

- alteração feita direto no banco não atualiza mais o `atualizado_em`
- o `updatable = false` continua necessário, porque a anotação diz quando
  preencher e não impede a coluna de entrar no `UPDATE`
- o flush no update continua, agora porque o `@LastModifiedDate` é preenchido no
  `@PreUpdate`, que o JPA dispara no flush
- a V3 cria e a V5 derruba, ida e volta visível no histórico

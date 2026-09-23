# LineUp

Sistema de gestão e agendamento para escola de surf.

Java 25, Spring Boot 4.1 e PostgreSQL 17.

## Rodando

Banco:

```bash
docker compose up -d
```

Copie `.env.example` para `.env`, preencha, e carregue antes de subir:

```bash
set -a && source .env && set +a
cd backend && ./mvnw spring-boot:run
```

A documentação da API fica em http://localhost:8080/swagger-ui.html, só no
profile `dev`.

## Testes

Precisam do Docker de pé: os testes de persistência rodam contra um Postgres
de verdade, em container.

```bash
cd backend && ./mvnw test
```

## Primeiro acesso

O super admin é inserido à mão no banco, uma vez. É a conta que cadastra as
escolas.

Gere o hash da senha. O comando pergunta a senha em vez de recebê-la como
argumento, para ela não ficar no histórico do shell:

```bash
htpasswd -nBC 10 ""
```

A saída começa com `:`. Use dali em diante, com o prefixo `{bcrypt}`:

```sql
INSERT INTO usuario (email, senha_hash, papel)
VALUES ('voce@exemplo.com', '{bcrypt}$2y$10$...', 'SUPER_ADMIN');
```

Depois disso o login responde em `POST /api/v1/auth/login`.

## Decisões

As decisões de arquitetura e o motivo de cada uma estão em [docs/adr](docs/adr).

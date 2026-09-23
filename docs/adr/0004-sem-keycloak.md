# 4. Autenticação na aplicação, sem Keycloak

Data: 2026-09-21

Antes de escrever o login eu avaliei usar Keycloak, que entrega login, refresh,
MFA, política de senha, login social e console de administração prontos.

O que pesou contra foi operação. Keycloak é uma aplicação Java com banco
próprio: em produção vira mais um deploy para atualizar e fazer backup. Para
três escolas e algumas dezenas de usuários, ele gasta mais recurso que o sistema
que vai proteger.

Tem um segundo ponto, menos visível. `escola_id` e `papel` são conceitos do
domínio e continuariam aqui no meu banco de qualquer jeito. A identidade ficaria
de um lado e o domínio do outro, e manter os dois em dia é problema conhecido.

Sobre o risco de fazer autenticação em casa, que é a objeção óbvia: o risco de
verdade é escrever criptografia, e não é o que está acontecendo. O hash de senha
e a emissão de JWT são do Spring Security, e a rotação segue a RFC 9700. O que
escrevi foi a ligação entre essas peças.

Posso me arrepender disso, e o gatilho é claro: uma segunda aplicação usando os
mesmos usuários, ou exigência de MFA, ou pedido de login com Google. Nesse dia a
troca não é do zero, porque a API valida JWT como resource server. Muda o
decoder, apaga o pacote de autenticação, e o resto do sistema nem fica sabendo
de onde o token passou a vir.

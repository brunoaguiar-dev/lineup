# 3. Refresh token opaco, com rotação

Data: 2026-09-21

O access token vale 15 minutos e não tem como ser cancelado antes disso. Encurtar
esse tempo protege mais e obriga o professor a logar toda hora. O refresh token
resolve os dois lados e ainda traz a revogação, que não existia.

Ele não é JWT. JWT serve para evitar consulta ao banco, e este token precisa ser
consultado toda vez, justamente porque tem que poder ser cancelado. É um valor
aleatório de 32 bytes do `SecureRandom`, guardado em hash SHA-256 pelo mesmo
motivo da senha.

Cada login abre uma família. Cada renovação cria um token novo na mesma família
e invalida o anterior. Se um token já usado aparecer de novo, existe cópia dele
em circulação. Não dá para saber qual das duas requisições é a do dono, então a
família inteira é revogada e os dois refazem o login. A
[RFC 9700](https://www.rfc-editor.org/rfc/rfc9700.html), na seção 4.14.2, chama
isso de rotação e exige para cliente que não consegue guardar segredo, que é o
caso de qualquer frontend no navegador.

Uma ressalva que vale registrar: essa RFC fala de servidor de autorização
OAuth2, e o que temos aqui é login de email e senha da própria aplicação. A
parte sobre refresh token se aplica igual, mas não somos um authorization
server, e é por isso que o Spring Authorization Server, que existe para
autenticar aplicações de terceiros, ficou de fora.

Os 15 minutos e os 7 dias são convenção. Procurei e a RFC não dá número nenhum.

O que isso custa:

- o refresh vai no corpo da resposta, então script injetado na página alcança
  ele. Cookie `httpOnly` seria melhor, mas traz CSRF de volta. Isso aqui eu
  reviso quando o frontend existir, porque hoje estaria decidindo no escuro
- token vencido não sai da tabela, que só cresce
- duas renovações ao mesmo tempo com o mesmo token derrubam a sessão, porque a
  segunda encontra o token consumido e trata como reuso

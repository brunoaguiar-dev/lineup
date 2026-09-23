# 2. Limite de tentativas no login por origem, não por conta

Data: 2026-09-21

O login é a única rota aberta da API. O bcrypt com custo 10 deixa cada tentativa
em uns 100ms, o que segura a velocidade, mas não impede ninguém de ficar
tentando a noite inteira.

Pensei primeiro em bloquear a conta depois de N erros, que é o que banco e
sistema corporativo fazem. Desisti por dois motivos.

O primeiro está na OWASP: quem souber o email de alguém consegue manter essa
pessoa fora do sistema errando a senha de propósito. O segundo é o custo. Seriam
três colunas novas em `usuario`, e a falha teria que ser gravada numa transação
separada, porque a exceção de credencial inválida faz rollback e levaria o
contador junto.

A outra opção é limitar requisições por origem, que é o que a web pública faz.
Não mexe na conta e não precisa de banco. Fiquei com essa.

Dez falhas em cinco minutos, contadas por IP e por email tentado, devolvem
`429`. Acertar a senha zera os contadores. São duas chaves porque cada uma
sozinha tem saída: só por IP, o atacante troca de origem; só por email, ele
varre vários emails do mesmo lugar. O estado fica em memória, num cache Caffeine
que expira a janela sozinho.

O que isso custa:

- reiniciar zera os contadores, o que não me preocupa porque o atacante não
  controla o reinício
- o limite vale por instância. Com mais de uma, cada uma conta a sua e isso aqui
  deixa de servir
- atrás de proxy o IP que chega é o do proxy, e todo mundo vira um cliente só.
  Existe `server.forward-headers-strategy` para resolver, mas só depois que
  houver proxy, senão é confiar num cabeçalho forjável
- ataque distribuído passa, e aí a barreira é o custo do bcrypt

# 5. Autorização declarada na anotação, decidida em Java

Data: 2026-09-22

Fui restringir as rotas de escola e caí em duas perguntas de naturezas
diferentes. Criar escola é só do super admin, e isso não depende de dado nenhum.
Ver e editar depende de comparar a escola do token com a escola da URL.

Pensei primeiro em escrever as duas dentro do `@PreAuthorize`. Desisti porque
expressão é texto: trocar `escolaId` por `escola_id` sem querer não dá erro,
muda quem entra. O compilador não olha ali e a IDE não renomeia junto.

A outra opção era levar a comparação para o corpo do service, onde o compilador
olha. Só que aí cada método passa a depender de eu lembrar de chamar a
verificação, e esquecer em um é um buraco que teste nenhum encontra.

Na documentação do Spring Security achei uma terceira, que foi a que usei. A
anotação fica, e chama um bean:

```java
@PreAuthorize("@acesso.naEscola(#id, authentication)")
```

A declaração continua encostada na rota, então não tem como esquecer, e a regra
é Java em classe própria, testada sem subir Spring. A mesma página desaconselha
expressão composta, o que já descarta `hasRole('X') or authentication.token...`.

Fica valendo para os módulos que vierem: papel puro na anotação, regra que
depende de dado vai para o bean.

O que isso custa:

- o nome do bean dentro da expressão é texto. Renomear a classe sem ajustar a
  anotação quebra em execução, não em compilação
- teste de slice de controller precisa importar o bean, senão a expressão não
  resolve

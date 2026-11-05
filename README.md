# Pilha em Java

[![CI](https://github.com/sidsidoso/pilha-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sidsidoso/pilha-java/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
[![Licença: MIT](https://img.shields.io/badge/licen%C3%A7a-MIT-blue.svg)](LICENSE)

Implementação de uma **pilha genérica (LIFO)** sobre array redimensionável, feita
do zero em Java 21 para a disciplina de Estrutura de Dados, junto com duas
aplicações clássicas que dependem de pilha:

- **verificador de parênteses balanceados** `()`, `[]` e `{}`;
- **avaliador de expressões infixas** com conversão para pós-fixa pelo algoritmo
  *shunting-yard*, suportando `+ - * / ^`, parênteses, precedência,
  associatividade e menos unário.

Sem dependências externas: compila com `javac` puro e os testes usam um mini
framework próprio.

## Conceito

Uma pilha só permite acesso a um extremo, o **topo**: o último elemento a
entrar é o primeiro a sair (*Last In, First Out*).

```
   empilhar(D)                         desempilhar() -> D

     ┌───┐                                  ┌───┐
     │ D │ <- topo                          │ D │ ──> sai
     ├───┤                                  └───┘
     │ C │                                  ┌───┐
     ├───┤                                  │ C │ <- topo
     │ B │                                  ├───┤
     ├───┤                                  │ B │
     │ A │ <- base                          ├───┤
     └───┘                                  │ A │ <- base
                                            └───┘
```

Internamente, os elementos ficam em um array; o topo é a posição `tamanho - 1`.
Empilhar e desempilhar mexem apenas no fim do array, por isso são O(1):

```
 índice:   0     1     2     3     4     5     6     7
         ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
 dados:  │  A  │  B  │  C  │  D  │null │null │null │null │   capacidade = 8
         └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
                             ▲
                             topo (tamanho = 4)
```

## Operações e complexidade

| Operação          | Descrição                                    | Tempo               |
|-------------------|----------------------------------------------|---------------------|
| `empilhar(x)`     | coloca `x` no topo                           | O(1) amortizado     |
| `desempilhar()`   | remove e devolve o topo                      | O(1) amortizado     |
| `topo()`          | devolve o topo sem remover                   | O(1)                |
| `tamanho()`       | quantidade de elementos                      | O(1)                |
| `vazia()`         | indica se não há elementos                   | O(1)                |
| `iterator()`      | percorre do topo para a base                 | O(n) no total       |
| `VerificadorParenteses.balanceado(s)` | checa `()[]{}`           | O(n)                |
| `AvaliadorExpressoes.avaliar(s)`      | infixa → pós-fixa → valor | O(n)                |

Memória: O(n). Graças à histerese, a capacidade fica sempre entre `n` e `4n`
(respeitando a capacidade mínima de 8).

`topo()` e `desempilhar()` em pilha vazia lançam `PilhaVaziaException`.

## Decisões de implementação e otimizações

- **Array redimensionável com dobra**: ao encher, a capacidade dobra. Cada
  elemento é copiado, em média, um número constante de vezes, o que garante
  O(1) amortizado.
- **Encolhimento com histerese**: o array só cai para a metade quando a
  ocupação chega a **1/4** (e nunca abaixo de 8). Se encolhesse já em 1/2, uma
  sequência alternada de empilhar/desempilhar na fronteira faria uma cópia a
  cada operação (*thrashing*). Com 1/4, depois de qualquer redimensionamento a
  pilha fica pela metade, longe dos dois limites.
- **Referências anuladas ao desempilhar**: a posição liberada recebe `null`.
  Sem isso, o array manteria vivos objetos que já saíram da pilha (*loitering*),
  impedindo o coletor de lixo de liberá-los.
- **Iterador fail-fast**: um contador de modificações detecta alterações durante
  a iteração e lança `ConcurrentModificationException`, como nas coleções do JDK.
- **Sem `null` como elemento**: `empilhar(null)` lança `NullPointerException`,
  evitando ambiguidade com posições vazias.
- **Verificador de parênteses**: empilha a *posição* de cada abertura, o que
  permite informar exatamente onde está o erro, não apenas se ele existe.
- **Shunting-yard**: operadores de maior precedência saem da pilha antes;
  `^` é associativo à direita (`2^3^2 = 2^9 = 512`) e os demais à esquerda
  (`100/10/2 = 5`). O menos unário é detectado pelo contexto (início, após
  operador ou `(`), vira o token `~` e tem precedência entre `* /` e `^`, de
  forma que `-2^2 = -4`, como na matemática.
- **Validação no tokenizador**: operandos e operadores binários precisam se
  alternar, então erros como `2 +`, `2 3` ou `()` são apontados com a posição.

## Estrutura de pastas

```
pilha-java/
├── .github/workflows/ci.yml        # CI: testes + demo no GitHub Actions
├── src/
│   ├── main/java/br/com/sidsidoso/pilha/
│   │   ├── Pilha.java              # a estrutura de dados
│   │   ├── PilhaVaziaException.java
│   │   ├── VerificadorParenteses.java
│   │   ├── AvaliadorExpressoes.java
│   │   └── Main.java               # demonstração
│   └── test/java/br/com/sidsidoso/pilha/
│       └── Testes.java             # mini framework de testes + casos
├── build.sh                        # build para Linux/macOS
├── build.ps1                       # build para Windows
├── LICENSE
└── README.md
```

## Como compilar, executar e testar

Pré-requisito: **JDK 21** ou superior (`javac` e `java` no `PATH`, ou
`JAVA_HOME` definido para o `build.ps1`).

### Com os scripts

```bash
# Linux / macOS
./build.sh          # só compila para out/
./build.sh run      # compila e executa a demonstração
./build.sh test     # compila e roda os testes
```

```powershell
# Windows (PowerShell)
.\build.ps1
.\build.ps1 run
.\build.ps1 test
```

Os scripts compilam com `-Xlint:all -Werror`, então qualquer aviso do
compilador quebra o build.

### Manualmente

```bash
javac --release 21 -Xlint:all -d out $(find src -name "*.java")
java -cp out br.com.sidsidoso.pilha.Main      # demonstração
java -cp out br.com.sidsidoso.pilha.Testes    # testes
```

O programa de testes imprime cada caso, termina com `N testes passaram` e sai
com código 1 se algum falhar. Entre os casos estão bordas (pilha vazia, um
elemento, crescimento e encolhimento, ausência de *thrashing*) e 200 mil
operações aleatórias comparadas com `java.util.ArrayDeque` como oráculo.

## Exemplo de saída

Saída real de `./build.sh run`:

```
1. Operacoes basicas
====================
empilhar(A)  -> [A]
empilhar(B)  -> [B, A]
empilhar(C)  -> [C, B, A]
empilhar(D)  -> [D, C, B, A]
topo()        -> D
desempilhar() -> D, pilha = [C, B, A]
tamanho()     -> 3, vazia() -> false
iterando (topo -> base): C B A
desempilhar() em pilha vazia -> PilhaVaziaException: impossivel desempilhar uma pilha vazia

Capacidade do array interno (cresce 2x; encolhe pela metade a 1/4):
  empilhando 1..40: 8 -> 16 (n=9) -> 32 (n=17) -> 64 (n=33)
  desempilhando tudo: 64 -> 32 (n=16) -> 16 (n=8) -> 8 (n=4)

2. Verificador de parenteses balanceados
========================================
"{[()()]}"                 balanceado
"(a + b) * [c - {d / e}]"  balanceado
"([)]"                     NAO balanceado (posicao 2, ')')
"((1 + 2)"                 NAO balanceado (posicao 0, '(')
"())"                      NAO balanceado (posicao 2, ')')
""                         balanceado

3. Avaliador de expressoes (shunting-yard)
==========================================
infixa  : 3 + 4 * 2
posfixa : 3 4 2 * +
valor   : 11

infixa  : (3 + 4) * 2
posfixa : 3 4 + 2 *
valor   : 14

infixa  : 3 + 4 * 2 / (1 - 5) ^ 2 ^ 3
posfixa : 3 4 2 * 1 5 - 2 3 ^ ^ / +
valor   : 3.0001220703125

infixa  : 2 ^ 3 ^ 2
posfixa : 2 3 2 ^ ^
valor   : 512

infixa  : 100 / 10 / 2
posfixa : 100 10 / 2 /
valor   : 5

infixa  : -2 ^ 2 + (-3) * 1.5
posfixa : 2 2 ^ ~ 3 ~ 1.5 * +
valor   : -8.5

infixa  : ((15 / (7 - (1 + 1))) * 3) - (2 + (1 + 1))
posfixa : 15 7 1 1 + - / 3 * 2 1 1 + + -
valor   : 5

Expressoes invalidas:
  "2 +"          -> expressao incompleta na posicao 3
  "(1 + 2"       -> '(' sem ')' correspondente
  "4 / (2 - 2)"  -> divisao por zero
  "3 # 4"        -> caractere invalido '#' na posicao 2
```

## Referências

- SEDGEWICK, R.; WAYNE, K. *Algorithms*. 4. ed. Addison-Wesley, 2011. Seção 1.3
  (pilhas com array redimensionável e avaliação de expressões).
- CORMEN, T. H. et al. *Algoritmos: teoria e prática*. 3. ed. Elsevier, 2012.
  Capítulo 10 (pilhas e filas) e Capítulo 17 (análise amortizada).
- DIJKSTRA, E. W. *Algol 60 translation*. Mathematisch Centrum, 1961
  (origem do algoritmo *shunting-yard*).
- [Shunting yard algorithm — Wikipedia](https://en.wikipedia.org/wiki/Shunting_yard_algorithm)
- [Documentação de `java.util.ArrayDeque`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html)

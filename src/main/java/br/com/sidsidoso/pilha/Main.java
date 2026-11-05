package br.com.sidsidoso.pilha;

import java.util.List;

/** Demonstracao da pilha e de suas aplicacoes. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        demoOperacoesBasicas();
        demoParenteses();
        demoExpressoes();
    }

    private static void demoOperacoesBasicas() {
        titulo("1. Operacoes basicas");
        Pilha<String> pilha = new Pilha<>();
        for (String s : List.of("A", "B", "C", "D")) {
            pilha.empilhar(s);
            System.out.printf("empilhar(%s)  -> %s%n", s, pilha);
        }
        System.out.printf("topo()        -> %s%n", pilha.topo());
        System.out.printf("desempilhar() -> %s, pilha = %s%n", pilha.desempilhar(), pilha);
        System.out.printf("tamanho()     -> %d, vazia() -> %b%n", pilha.tamanho(), pilha.vazia());

        System.out.print("iterando (topo -> base):");
        for (String s : pilha) {
            System.out.print(" " + s);
        }
        System.out.println();

        while (!pilha.vazia()) {
            pilha.desempilhar();
        }
        try {
            pilha.desempilhar();
        } catch (PilhaVaziaException e) {
            System.out.println("desempilhar() em pilha vazia -> PilhaVaziaException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Capacidade do array interno (cresce 2x; encolhe pela metade a 1/4):");
        Pilha<Integer> numeros = new Pilha<>();
        StringBuilder trilha = new StringBuilder("  empilhando 1..40: ");
        int ultima = numeros.capacidade();
        trilha.append(ultima);
        for (int i = 1; i <= 40; i++) {
            numeros.empilhar(i);
            if (numeros.capacidade() != ultima) {
                ultima = numeros.capacidade();
                trilha.append(" -> ").append(ultima).append(" (n=").append(i).append(')');
            }
        }
        System.out.println(trilha);
        trilha = new StringBuilder("  desempilhando tudo: ").append(ultima);
        while (!numeros.vazia()) {
            numeros.desempilhar();
            if (numeros.capacidade() != ultima) {
                ultima = numeros.capacidade();
                trilha.append(" -> ").append(ultima).append(" (n=").append(numeros.tamanho()).append(')');
            }
        }
        System.out.println(trilha);
    }

    private static void demoParenteses() {
        titulo("2. Verificador de parenteses balanceados");
        String[] exemplos = {"{[()()]}", "(a + b) * [c - {d / e}]", "([)]", "((1 + 2)", "())", ""};
        for (String texto : exemplos) {
            int erro = VerificadorParenteses.posicaoDoErro(texto);
            String rotulo = String.format("%-26s", '"' + texto + '"');
            if (erro == -1) {
                System.out.println(rotulo + " balanceado");
            } else {
                System.out.printf("%s NAO balanceado (posicao %d, '%c')%n", rotulo, erro, texto.charAt(erro));
            }
        }
    }

    private static void demoExpressoes() {
        titulo("3. Avaliador de expressoes (shunting-yard)");
        String[] expressoes = {
            "3 + 4 * 2",
            "(3 + 4) * 2",
            "3 + 4 * 2 / (1 - 5) ^ 2 ^ 3",
            "2 ^ 3 ^ 2",
            "100 / 10 / 2",
            "-2 ^ 2 + (-3) * 1.5",
            "((15 / (7 - (1 + 1))) * 3) - (2 + (1 + 1))",
        };
        for (String expr : expressoes) {
            List<String> posfixa = AvaliadorExpressoes.paraPosfixa(expr);
            System.out.println("infixa  : " + expr);
            System.out.println("posfixa : " + String.join(" ", posfixa));
            System.out.println("valor   : " + formatar(AvaliadorExpressoes.avaliarPosfixa(posfixa)));
            System.out.println();
        }
        System.out.println("Expressoes invalidas:");
        for (String expr : new String[] {"2 +", "(1 + 2", "4 / (2 - 2)", "3 # 4"}) {
            try {
                AvaliadorExpressoes.avaliar(expr);
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.printf("  %-14s -> %s%n", '"' + expr + '"', e.getMessage());
            }
        }
    }

    private static String formatar(double valor) {
        if (valor == Math.rint(valor) && !Double.isInfinite(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
    }
}

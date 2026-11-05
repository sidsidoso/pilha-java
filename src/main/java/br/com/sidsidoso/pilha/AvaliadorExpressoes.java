package br.com.sidsidoso.pilha;

import java.util.ArrayList;
import java.util.List;

/**
 * Avalia expressoes aritmeticas infixas em duas etapas, ambas com pilha:
 * <ol>
 *   <li>conversao infixa para pos-fixa pelo algoritmo shunting-yard (Dijkstra);</li>
 *   <li>avaliacao da forma pos-fixa (notacao polonesa reversa).</li>
 * </ol>
 *
 * <p>Suporta numeros decimais, {@code + - * / ^}, parenteses e menos unario.
 * Precedencia (maior primeiro): {@code ^} (associativo a direita), menos unario,
 * {@code * /}, {@code + -} (os demais associativos a esquerda). Assim,
 * {@code -2^2 = -4} e {@code 2^3^2 = 512}. Na forma pos-fixa o menos unario e
 * representado por {@value #MENOS_UNARIO}.
 *
 * <p>Expressoes invalidas geram {@link IllegalArgumentException}; divisao por
 * zero gera {@link ArithmeticException}.
 */
public final class AvaliadorExpressoes {

    /** Token que representa o menos unario na forma pos-fixa. */
    public static final String MENOS_UNARIO = "~";

    private AvaliadorExpressoes() {
    }

    /**
     * Avalia uma expressao infixa.
     *
     * @param expressao expressao, ex.: {@code "3 + 4 * (2 - 1)"}
     * @return valor da expressao
     */
    public static double avaliar(String expressao) {
        return avaliarPosfixa(paraPosfixa(expressao));
    }

    /**
     * Separa a expressao em tokens (numeros, operadores e parenteses) e valida
     * a sequencia: operandos e operadores binarios devem se alternar.
     *
     * @param expressao expressao infixa
     * @return lista de tokens; o menos unario vira {@value #MENOS_UNARIO}
     */
    public static List<String> tokenizar(String expressao) {
        List<String> tokens = new ArrayList<>();
        boolean esperaOperando = true;
        int i = 0;
        while (i < expressao.length()) {
            char c = expressao.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                if (!esperaOperando) {
                    throw erro("operador ausente", i);
                }
                int inicio = i;
                boolean temPonto = false;
                while (i < expressao.length()
                        && (Character.isDigit(expressao.charAt(i)) || (expressao.charAt(i) == '.' && !temPonto))) {
                    temPonto |= expressao.charAt(i) == '.';
                    i++;
                }
                String numero = expressao.substring(inicio, i);
                if (numero.equals(".")) {
                    throw erro("numero invalido", inicio);
                }
                tokens.add(numero);
                esperaOperando = false;
                continue;
            }
            if (c == '-' && esperaOperando) {
                tokens.add(MENOS_UNARIO);
            } else if (c == '(') {
                if (!esperaOperando) {
                    throw erro("operador ausente", i);
                }
                tokens.add("(");
            } else if (c == ')') {
                if (esperaOperando) {
                    throw erro("operando ausente", i);
                }
                tokens.add(")");
            } else if (ehOperadorBinario(c)) {
                if (esperaOperando) {
                    throw erro("operando ausente", i);
                }
                tokens.add(String.valueOf(c));
                esperaOperando = true;
            } else {
                throw erro("caractere invalido '" + c + "'", i);
            }
            i++;
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("expressao vazia");
        }
        if (esperaOperando) {
            throw erro("expressao incompleta", expressao.length());
        }
        return tokens;
    }

    /**
     * Converte uma expressao infixa para a forma pos-fixa (shunting-yard).
     *
     * @param expressao expressao infixa
     * @return tokens em ordem pos-fixa, ex.: {@code [3, 4, 2, *, +]}
     */
    public static List<String> paraPosfixa(String expressao) {
        List<String> saida = new ArrayList<>();
        Pilha<String> operadores = new Pilha<>();
        for (String token : tokenizar(expressao)) {
            if (ehNumero(token)) {
                saida.add(token);
            } else if (token.equals("(") || token.equals(MENOS_UNARIO)) {
                // operadores prefixos entram direto, sem desempilhar ninguem
                operadores.empilhar(token);
            } else if (token.equals(")")) {
                while (!operadores.vazia() && !operadores.topo().equals("(")) {
                    saida.add(operadores.desempilhar());
                }
                if (operadores.vazia()) {
                    throw new IllegalArgumentException("')' sem '(' correspondente");
                }
                operadores.desempilhar(); // descarta o "("
            } else {
                while (!operadores.vazia() && !operadores.topo().equals("(")
                        && deveSairAntes(operadores.topo(), token)) {
                    saida.add(operadores.desempilhar());
                }
                operadores.empilhar(token);
            }
        }
        while (!operadores.vazia()) {
            String op = operadores.desempilhar();
            if (op.equals("(")) {
                throw new IllegalArgumentException("'(' sem ')' correspondente");
            }
            saida.add(op);
        }
        return saida;
    }

    /**
     * Avalia uma expressao ja em forma pos-fixa usando uma pilha de operandos.
     *
     * @param posfixa tokens em ordem pos-fixa
     * @return valor da expressao
     */
    public static double avaliarPosfixa(List<String> posfixa) {
        Pilha<Double> operandos = new Pilha<>();
        try {
            for (String token : posfixa) {
                if (ehNumero(token)) {
                    operandos.empilhar(Double.parseDouble(token));
                } else if (token.equals(MENOS_UNARIO)) {
                    operandos.empilhar(-operandos.desempilhar());
                } else {
                    double direito = operandos.desempilhar();
                    double esquerdo = operandos.desempilhar();
                    operandos.empilhar(aplicar(token.charAt(0), esquerdo, direito));
                }
            }
            double resultado = operandos.desempilhar();
            if (!operandos.vazia()) {
                throw new IllegalArgumentException("expressao pos-fixa malformada: operandos sobrando");
            }
            return resultado;
        } catch (PilhaVaziaException e) {
            throw new IllegalArgumentException("expressao pos-fixa malformada: operandos insuficientes", e);
        }
    }

    private static double aplicar(char operador, double a, double b) {
        return switch (operador) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) {
                    throw new ArithmeticException("divisao por zero");
                }
                yield a / b;
            }
            case '^' -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("operador desconhecido: " + operador);
        };
    }

    /** O operador do topo sai antes de empilhar {@code novo}? */
    private static boolean deveSairAntes(String topo, String novo) {
        int pTopo = precedencia(topo);
        int pNovo = precedencia(novo);
        return pTopo > pNovo || (pTopo == pNovo && !novo.equals("^"));
    }

    private static int precedencia(String operador) {
        return switch (operador) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case MENOS_UNARIO -> 3;
            case "^" -> 4;
            default -> throw new IllegalArgumentException("operador desconhecido: " + operador);
        };
    }

    private static boolean ehOperadorBinario(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private static boolean ehNumero(String token) {
        char c = token.charAt(0);
        return Character.isDigit(c) || c == '.';
    }

    private static IllegalArgumentException erro(String motivo, int posicao) {
        return new IllegalArgumentException(motivo + " na posicao " + posicao);
    }
}

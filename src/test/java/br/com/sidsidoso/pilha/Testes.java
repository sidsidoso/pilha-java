package br.com.sidsidoso.pilha;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/**
 * Testes com um mini framework proprio (sem JUnit). Cada teste e um metodo
 * estatico registrado em {@link #main}; uma falha de assercao lanca
 * {@link AssertionError}. Sai com codigo 1 se algum teste falhar.
 */
public final class Testes {

    private static int passaram;
    private static final List<String> falhas = new ArrayList<>();

    private Testes() {
    }

    public static void main(String[] args) {
        // Pilha
        teste("pilha nova esta vazia", Testes::pilhaNovaVazia);
        teste("topo/desempilhar em pilha vazia lancam excecao", Testes::operacoesEmPilhaVazia);
        teste("pilha com um elemento", Testes::umElemento);
        teste("ordem LIFO", Testes::ordemLifo);
        teste("rejeita null", Testes::rejeitaNull);
        teste("cresce dobrando a capacidade", Testes::crescimento);
        teste("encolhe com histerese (1/4 -> metade)", Testes::encolhimento);
        teste("nunca encolhe abaixo da capacidade minima", Testes::capacidadeMinima);
        teste("sem thrashing na fronteira de capacidade", Testes::semThrashing);
        teste("anula referencia ao desempilhar", Testes::anulaReferencia);
        teste("iterador vai do topo para a base", Testes::iteradorTopoBase);
        teste("iterador em pilha vazia", Testes::iteradorVazio);
        teste("iterador e fail-fast", Testes::iteradorFailFast);
        teste("toString do topo para a base", Testes::representacaoTexto);
        teste("operacoes aleatorias vs ArrayDeque (oraculo)", Testes::aleatorioContraArrayDeque);
        // Parenteses
        teste("parenteses balanceados", Testes::parentesesBalanceados);
        teste("parenteses nao balanceados", Testes::parentesesNaoBalanceados);
        teste("posicao do erro de parenteses", Testes::posicaoErroParenteses);
        // Expressoes
        teste("conversao infixa -> pos-fixa", Testes::conversaoPosfixa);
        teste("precedencia de operadores", Testes::precedencia);
        teste("associatividade (esquerda e ^ a direita)", Testes::associatividade);
        teste("parenteses alteram a ordem", Testes::parentesesEmExpressoes);
        teste("decimais e menos unario", Testes::decimaisEMenosUnario);
        teste("numero isolado", Testes::numeroIsolado);
        teste("expressoes invalidas", Testes::expressoesInvalidas);
        teste("divisao por zero", Testes::divisaoPorZero);
        teste("pos-fixa malformada", Testes::posfixaMalformada);

        System.out.println();
        if (!falhas.isEmpty()) {
            System.out.println(falhas.size() + " teste(s) falharam:");
            falhas.forEach(f -> System.out.println("  - " + f));
            System.out.println(passaram + " testes passaram");
            System.exit(1);
        }
        System.out.println(passaram + " testes passaram");
    }

    // ------------------------------------------------------------ mini framework

    private interface CorpoTeste {
        void executar() throws Exception;
    }

    private static void teste(String nome, CorpoTeste corpo) {
        try {
            corpo.executar();
            passaram++;
            System.out.println("[ OK ] " + nome);
        } catch (Throwable t) {
            falhas.add(nome + ": " + t);
            System.out.println("[FALHOU] " + nome + " -> " + t);
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void igual(Object esperado, Object obtido) {
        if (!Objects.equals(esperado, obtido)) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + obtido + ">");
        }
    }

    private static void proximo(double esperado, double obtido) {
        if (Math.abs(esperado - obtido) > 1e-9) {
            throw new AssertionError("esperado <" + esperado + "> mas foi <" + obtido + ">");
        }
    }

    private static <E extends Throwable> E lanca(Class<E> tipo, CorpoTeste corpo) {
        try {
            corpo.executar();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                return tipo.cast(t);
            }
            throw new AssertionError("esperava " + tipo.getSimpleName() + " mas foi " + t, t);
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + " mas nada foi lancado");
    }

    // ------------------------------------------------------------ pilha

    private static void pilhaNovaVazia() {
        Pilha<Integer> p = new Pilha<>();
        verificar(p.vazia(), "deveria estar vazia");
        igual(0, p.tamanho());
        igual(Pilha.CAPACIDADE_MINIMA, p.capacidade());
    }

    private static void operacoesEmPilhaVazia() {
        Pilha<Integer> p = new Pilha<>();
        lanca(PilhaVaziaException.class, p::topo);
        lanca(PilhaVaziaException.class, p::desempilhar);
        p.empilhar(1);
        p.desempilhar();
        lanca(PilhaVaziaException.class, p::desempilhar);
    }

    private static void umElemento() {
        Pilha<String> p = new Pilha<>();
        p.empilhar("x");
        igual(1, p.tamanho());
        verificar(!p.vazia(), "nao deveria estar vazia");
        igual("x", p.topo());
        igual(1, p.tamanho());
        igual("x", p.desempilhar());
        verificar(p.vazia(), "deveria ficar vazia");
    }

    private static void ordemLifo() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 0; i < 100; i++) {
            p.empilhar(i);
        }
        for (int i = 99; i >= 0; i--) {
            igual(i, p.desempilhar());
        }
    }

    private static void rejeitaNull() {
        Pilha<String> p = new Pilha<>();
        lanca(NullPointerException.class, () -> p.empilhar(null));
        verificar(p.vazia(), "null nao pode ser inserido");
    }

    private static void crescimento() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 1; i <= 8; i++) {
            p.empilhar(i);
        }
        igual(8, p.capacidade());
        p.empilhar(9);
        igual(16, p.capacidade());
        for (int i = 10; i <= 1000; i++) {
            p.empilhar(i);
        }
        igual(1024, p.capacidade());
        igual(1000, p.tamanho());
        igual(1000, p.topo());
    }

    private static void encolhimento() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 0; i < 64; i++) {
            p.empilhar(i);
        }
        igual(64, p.capacidade());
        while (p.tamanho() > 17) {
            p.desempilhar();
        }
        igual(64, p.capacidade()); // 17 > 64/4: ainda nao encolhe
        p.desempilhar();
        igual(32, p.capacidade()); // 16 == 64/4: encolhe para a metade
        while (p.tamanho() > 8) {
            p.desempilhar();
        }
        igual(16, p.capacidade());
        for (int i = 7; i >= 0; i--) {
            igual(i, p.desempilhar()); // conteudo preservado apos as copias
        }
    }

    private static void capacidadeMinima() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 0; i < 9; i++) {
            p.empilhar(i);
        }
        while (!p.vazia()) {
            p.desempilhar();
        }
        igual(Pilha.CAPACIDADE_MINIMA, p.capacidade());
    }

    private static void semThrashing() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 0; i < 16; i++) {
            p.empilhar(i);
        }
        igual(16, p.capacidade());
        // oscilar em torno de 16/17 nao deve realocar a cada operacao
        p.empilhar(16);
        igual(32, p.capacidade());
        for (int i = 0; i < 50; i++) {
            p.desempilhar();
            igual(32, p.capacidade());
            p.empilhar(i);
            igual(32, p.capacidade());
        }
    }

    private static void anulaReferencia() throws Exception {
        Pilha<String> p = new Pilha<>();
        p.empilhar("a");
        p.empilhar("b");
        p.desempilhar();
        Field campo = Pilha.class.getDeclaredField("dados");
        campo.setAccessible(true);
        Object[] dados = (Object[]) campo.get(p);
        igual("a", dados[0]);
        igual(null, dados[1]);
    }

    private static void iteradorTopoBase() {
        Pilha<Integer> p = new Pilha<>();
        for (int i = 1; i <= 20; i++) {
            p.empilhar(i);
        }
        int esperado = 20;
        for (int x : p) {
            igual(esperado--, x);
        }
        igual(0, esperado);
        igual(20, p.tamanho()); // iterar nao remove
    }

    private static void iteradorVazio() {
        Iterator<String> it = new Pilha<String>().iterator();
        verificar(!it.hasNext(), "iterador de pilha vazia nao tem proximo");
        lanca(NoSuchElementException.class, it::next);
    }

    private static void iteradorFailFast() {
        Pilha<Integer> p = new Pilha<>();
        p.empilhar(1);
        p.empilhar(2);
        Iterator<Integer> it = p.iterator();
        it.next();
        p.empilhar(3);
        lanca(ConcurrentModificationException.class, it::next);
    }

    private static void representacaoTexto() {
        Pilha<Integer> p = new Pilha<>();
        igual("[]", p.toString());
        p.empilhar(1);
        p.empilhar(2);
        p.empilhar(3);
        igual("[3, 2, 1]", p.toString());
    }

    private static void aleatorioContraArrayDeque() {
        Random rnd = new Random(42);
        Pilha<Integer> pilha = new Pilha<>();
        ArrayDeque<Integer> oraculo = new ArrayDeque<>();
        for (int passo = 0; passo < 200_000; passo++) {
            // fases que favorecem crescer ou encolher, para exercitar o redimensionamento
            boolean fasePush = (passo / 5_000) % 2 == 0;
            int r = rnd.nextInt(100);
            if (r < (fasePush ? 60 : 35)) {
                int x = rnd.nextInt();
                pilha.empilhar(x);
                oraculo.push(x);
            } else if (r < 90) {
                if (oraculo.isEmpty()) {
                    lanca(PilhaVaziaException.class, pilha::desempilhar);
                } else {
                    igual(oraculo.pop(), pilha.desempilhar());
                }
            } else if (oraculo.isEmpty()) {
                lanca(PilhaVaziaException.class, pilha::topo);
            } else {
                igual(oraculo.peek(), pilha.topo());
            }
            igual(oraculo.size(), pilha.tamanho());
            igual(oraculo.isEmpty(), pilha.vazia());
            verificar(pilha.capacidade() >= pilha.tamanho(), "capacidade menor que o tamanho");
        }
        // mesma ordem de iteracao (ArrayDeque.push tambem itera do topo para a base)
        Iterator<Integer> esperado = oraculo.iterator();
        for (int x : pilha) {
            igual(esperado.next(), x);
        }
        verificar(!esperado.hasNext(), "iteracao com tamanhos diferentes");
    }

    // ------------------------------------------------------------ parenteses

    private static void parentesesBalanceados() {
        for (String s : List.of("", "()", "[]", "{}", "{[()()]}", "(a + [b * {c}])", "texto sem delimitadores")) {
            verificar(VerificadorParenteses.balanceado(s), "deveria estar balanceado: " + s);
        }
    }

    private static void parentesesNaoBalanceados() {
        for (String s : List.of("(", ")", "([)]", "((", "{[}]", "(()", "())", "}{")) {
            verificar(!VerificadorParenteses.balanceado(s), "nao deveria estar balanceado: " + s);
        }
    }

    private static void posicaoErroParenteses() {
        igual(-1, VerificadorParenteses.posicaoDoErro("(a)[b]{c}"));
        igual(2, VerificadorParenteses.posicaoDoErro("([)]"));  // ')' nao fecha '['
        igual(2, VerificadorParenteses.posicaoDoErro("())"));   // ')' sobrando
        igual(0, VerificadorParenteses.posicaoDoErro("((1)"));  // o '(' do indice 1 casou; sobra o 0
        igual(3, VerificadorParenteses.posicaoDoErro("(()(")); // sobram 0 e 3; 3 e o mais interno
        igual(0, VerificadorParenteses.posicaoDoErro("]"));
    }

    // ------------------------------------------------------------ expressoes

    private static String posfixa(String expr) {
        return String.join(" ", AvaliadorExpressoes.paraPosfixa(expr));
    }

    private static void conversaoPosfixa() {
        igual("3 4 2 * +", posfixa("3 + 4 * 2"));
        igual("3 4 + 2 *", posfixa("(3 + 4) * 2"));
        igual("3 4 2 * 1 5 - 2 3 ^ ^ / +", posfixa("3 + 4 * 2 / ( 1 - 5 ) ^ 2 ^ 3"));
        igual("2 ~ 2 ^", posfixa("(-2) ^ 2"));
        igual("2 2 ^ ~", posfixa("-2 ^ 2"));
    }

    private static void precedencia() {
        proximo(11, AvaliadorExpressoes.avaliar("3 + 4 * 2"));
        proximo(7, AvaliadorExpressoes.avaliar("1 + 12 / 2"));
        proximo(19, AvaliadorExpressoes.avaliar("3 + 2 ^ 4"));
        proximo(48, AvaliadorExpressoes.avaliar("3 * 2 ^ 4"));
        proximo(3.0001220703125, AvaliadorExpressoes.avaliar("3 + 4 * 2 / (1 - 5) ^ 2 ^ 3"));
    }

    private static void associatividade() {
        proximo(3, AvaliadorExpressoes.avaliar("8 - 3 - 2"));
        proximo(5, AvaliadorExpressoes.avaliar("100 / 10 / 2"));
        proximo(512, AvaliadorExpressoes.avaliar("2 ^ 3 ^ 2"));
        proximo(64, AvaliadorExpressoes.avaliar("(2 ^ 3) ^ 2"));
    }

    private static void parentesesEmExpressoes() {
        proximo(14, AvaliadorExpressoes.avaliar("(3 + 4) * 2"));
        proximo(5, AvaliadorExpressoes.avaliar("((15 / (7 - (1 + 1))) * 3) - (2 + (1 + 1))"));
        proximo(7, AvaliadorExpressoes.avaliar("(((7)))"));
    }

    private static void decimaisEMenosUnario() {
        proximo(0.75, AvaliadorExpressoes.avaliar("1.5 * .5"));
        proximo(-4, AvaliadorExpressoes.avaliar("-2 ^ 2"));
        proximo(4, AvaliadorExpressoes.avaliar("(-2) ^ 2"));
        proximo(-6, AvaliadorExpressoes.avaliar("2 * -3"));
        proximo(0.25, AvaliadorExpressoes.avaliar("2 ^ -2"));
        proximo(3, AvaliadorExpressoes.avaliar("--3"));
        proximo(-1, AvaliadorExpressoes.avaliar("-(3 - 2)"));
    }

    private static void numeroIsolado() {
        proximo(42, AvaliadorExpressoes.avaliar("42"));
        igual(List.of("42"), AvaliadorExpressoes.paraPosfixa("  42  "));
    }

    private static void expressoesInvalidas() {
        for (String s : List.of("", "   ", "2 +", "* 2", "(1 + 2", "1 + 2)", "()", "2 3", "2 (3)", "3 # 4", ".", "1.2.3")) {
            lanca(IllegalArgumentException.class, () -> AvaliadorExpressoes.avaliar(s));
        }
    }

    private static void divisaoPorZero() {
        lanca(ArithmeticException.class, () -> AvaliadorExpressoes.avaliar("4 / (2 - 2)"));
    }

    private static void posfixaMalformada() {
        lanca(IllegalArgumentException.class, () -> AvaliadorExpressoes.avaliarPosfixa(List.of("1", "+")));
        lanca(IllegalArgumentException.class, () -> AvaliadorExpressoes.avaliarPosfixa(List.of("1", "2")));
        lanca(IllegalArgumentException.class, () -> AvaliadorExpressoes.avaliarPosfixa(List.of()));
        proximo(7, AvaliadorExpressoes.avaliarPosfixa(List.of("1", "2", "3", "*", "+")));
    }
}

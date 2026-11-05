package br.com.sidsidoso.pilha;

/**
 * Verifica se os delimitadores {@code ()}, {@code []} e {@code {}} de um texto
 * estao balanceados e corretamente aninhados. Demais caracteres sao ignorados.
 *
 * <p>Cada abertura e empilhada (pela sua posicao); cada fechamento precisa
 * casar com o topo. Complexidade O(n) em tempo e O(n) em espaco no pior caso.
 */
public final class VerificadorParenteses {

    private VerificadorParenteses() {
    }

    /**
     * @param texto texto a verificar
     * @return {@code true} se todos os delimitadores estiverem balanceados
     */
    public static boolean balanceado(String texto) {
        return posicaoDoErro(texto) == -1;
    }

    /**
     * Localiza o primeiro problema de balanceamento.
     *
     * @param texto texto a verificar
     * @return -1 se estiver balanceado; caso contrario, o indice do fechamento
     *         que nao casa ou, se sobrarem aberturas, o indice da abertura mais
     *         interna sem par
     */
    public static int posicaoDoErro(String texto) {
        Pilha<Integer> aberturas = new Pilha<>();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (ehAbertura(c)) {
                aberturas.empilhar(i);
            } else if (ehFechamento(c)) {
                if (aberturas.vazia() || texto.charAt(aberturas.topo()) != aberturaDe(c)) {
                    return i;
                }
                aberturas.desempilhar();
            }
        }
        return aberturas.vazia() ? -1 : aberturas.topo();
    }

    private static boolean ehAbertura(char c) {
        return c == '(' || c == '[' || c == '{';
    }

    private static boolean ehFechamento(char c) {
        return c == ')' || c == ']' || c == '}';
    }

    private static char aberturaDe(char fechamento) {
        return switch (fechamento) {
            case ')' -> '(';
            case ']' -> '[';
            default -> '{';
        };
    }
}

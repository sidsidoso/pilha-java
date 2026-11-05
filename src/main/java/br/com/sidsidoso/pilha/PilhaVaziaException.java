package br.com.sidsidoso.pilha;

/** Lancada ao tentar desempilhar ou consultar o topo de uma pilha vazia. */
public class PilhaVaziaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** @param mensagem descricao do erro */
    public PilhaVaziaException(String mensagem) {
        super(mensagem);
    }
}

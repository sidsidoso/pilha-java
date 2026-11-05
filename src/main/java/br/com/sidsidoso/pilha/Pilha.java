package br.com.sidsidoso.pilha;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pilha generica (LIFO) implementada sobre um array redimensionavel.
 *
 * <p>O array dobra de tamanho quando enche e cai pela metade quando a ocupacao
 * chega a 1/4 da capacidade (histerese), o que evita realocacoes sucessivas
 * quando a pilha oscila em torno de uma potencia de dois. Todas as operacoes
 * sao O(1) amortizado.
 *
 * <p>Nao aceita {@code null} como elemento. Nao e thread-safe.
 *
 * @param <T> tipo dos elementos
 */
public final class Pilha<T> implements Iterable<T> {

    /** Capacidade inicial e capacidade minima do array interno. */
    static final int CAPACIDADE_MINIMA = 8;

    private Object[] dados;
    private int tamanho;
    /** Contador de modificacoes estruturais, usado pelo iterador fail-fast. */
    private int modificacoes;

    /** Cria uma pilha vazia com a capacidade minima. */
    public Pilha() {
        dados = new Object[CAPACIDADE_MINIMA];
    }

    /**
     * Coloca um elemento no topo da pilha.
     *
     * @param elemento elemento a empilhar (nao nulo)
     * @throws NullPointerException se o elemento for nulo
     */
    public void empilhar(T elemento) {
        if (elemento == null) {
            throw new NullPointerException("a pilha nao aceita elementos nulos");
        }
        if (tamanho == dados.length) {
            redimensionar(dados.length * 2);
        }
        dados[tamanho++] = elemento;
        modificacoes++;
    }

    /**
     * Remove e devolve o elemento do topo.
     *
     * @return elemento que estava no topo
     * @throws PilhaVaziaException se a pilha estiver vazia
     */
    public T desempilhar() {
        exigirNaoVazia("desempilhar");
        T elemento = elementoEm(--tamanho);
        dados[tamanho] = null; // libera a referencia para o coletor de lixo
        modificacoes++;
        if (tamanho > 0 && tamanho == dados.length / 4 && dados.length > CAPACIDADE_MINIMA) {
            redimensionar(dados.length / 2);
        }
        return elemento;
    }

    /**
     * Devolve o elemento do topo sem remove-lo.
     *
     * @return elemento do topo
     * @throws PilhaVaziaException se a pilha estiver vazia
     */
    public T topo() {
        exigirNaoVazia("consultar o topo de");
        return elementoEm(tamanho - 1);
    }

    /** @return quantidade de elementos na pilha */
    public int tamanho() {
        return tamanho;
    }

    /** @return {@code true} se a pilha nao tiver elementos */
    public boolean vazia() {
        return tamanho == 0;
    }

    /** @return capacidade atual do array interno (visivel para testes) */
    int capacidade() {
        return dados.length;
    }

    /**
     * Itera do topo para a base, sem remover elementos. O iterador e
     * fail-fast: lanca {@link ConcurrentModificationException} se a pilha for
     * modificada durante a iteracao.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int proximo = tamanho - 1;
            private final int modificacoesEsperadas = modificacoes;

            @Override
            public boolean hasNext() {
                return proximo >= 0;
            }

            @Override
            public T next() {
                if (modificacoes != modificacoesEsperadas) {
                    throw new ConcurrentModificationException();
                }
                if (proximo < 0) {
                    throw new NoSuchElementException();
                }
                return elementoEm(proximo--);
            }
        };
    }

    /** @return representacao do topo para a base, ex.: {@code [3, 2, 1]} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = tamanho - 1; i >= 0; i--) {
            sb.append(dados[i]);
            if (i > 0) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }

    private void redimensionar(int novaCapacidade) {
        Object[] novo = new Object[novaCapacidade];
        System.arraycopy(dados, 0, novo, 0, tamanho);
        dados = novo;
    }

    private void exigirNaoVazia(String operacao) {
        if (tamanho == 0) {
            throw new PilhaVaziaException("impossivel " + operacao + " uma pilha vazia");
        }
    }

    @SuppressWarnings("unchecked")
    private T elementoEm(int indice) {
        return (T) dados[indice];
    }
}

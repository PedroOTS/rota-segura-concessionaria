package br.rotasegura.repositorio;

import br.rotasegura.excecao.DadosInvalidosException;
import br.rotasegura.excecao.EntidadeNaoEncontradaException;
import br.rotasegura.modelo.Identificavel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * TIPOS GENÉRICOS: um único repositório reutilizado para Veiculo, Cliente e Contrato.
 * T é limitado a Identificavel (bounded type parameter).
 */
public class Repositorio<T extends Identificavel> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, T> itens = new LinkedHashMap<>();

    public void adicionar(T item) throws DadosInvalidosException {
        if (itens.containsKey(item.getId())) {
            throw new DadosInvalidosException("Já existe um registro com o identificador " + item.getId() + ".");
        }
        itens.put(item.getId(), item);
    }

    public T buscar(String id) throws EntidadeNaoEncontradaException {
        T item = itens.get(id);
        if (item == null) throw new EntidadeNaoEncontradaException("Registro não encontrado: " + id);
        return item;
    }

    public List<T> listar() { return new ArrayList<>(itens.values()); }

    public List<T> filtrar(Predicate<T> criterio) {
        List<T> r = new ArrayList<>();
        for (T t : itens.values()) if (criterio.test(t)) r.add(t);
        return r;
    }
}

package br.rotasegura.persistencia;

import br.rotasegura.modelo.Cliente;
import br.rotasegura.modelo.Contrato;
import br.rotasegura.modelo.Veiculo;
import br.rotasegura.repositorio.Repositorio;
import java.io.Serializable;

/** Raiz do grafo de objetos salvo em disco (um só arquivo binário). */
public class BancoDeDados implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Repositorio<Veiculo> veiculos = new Repositorio<>();
    public final Repositorio<Cliente> clientes = new Repositorio<>();
    public final Repositorio<Contrato> contratos = new Repositorio<>();
    public int sequenciaContrato = 0;
}

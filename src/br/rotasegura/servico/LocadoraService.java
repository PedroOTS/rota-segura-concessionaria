package br.rotasegura.servico;

import br.rotasegura.excecao.*;
import br.rotasegura.modelo.*;
import br.rotasegura.persistencia.ArquivoPersistencia;
import br.rotasegura.persistencia.BancoDeDados;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Regras de negócio da locadora. A interface com o usuário (Main) só chama estes métodos. */
public class LocadoraService {
    private final ArquivoPersistencia persistencia;
    private final BancoDeDados banco;

    public LocadoraService(ArquivoPersistencia persistencia) throws IOException, ClassNotFoundException {
        this.persistencia = persistencia;
        this.banco = persistencia.carregar();
    }

    public void cadastrarVeiculo(Veiculo v) throws RotaSeguraException, IOException {
        banco.veiculos.adicionar(v);
        persistencia.salvar(banco);
    }

    public void cadastrarCliente(Cliente c) throws RotaSeguraException, IOException {
        banco.clientes.adicionar(c);
        persistencia.salvar(banco);
    }

    public Contrato abrirLocacao(String cpf, String placa, LocalDate retirada, LocalDate prevista)
            throws RotaSeguraException, IOException {
        if (retirada == null || prevista == null) throw new DataInvalidaException("Datas obrigatórias.");
        if (retirada.isBefore(LocalDate.now())) throw new DataInvalidaException("A retirada não pode ser no passado.");
        if (!prevista.isAfter(retirada)) throw new DataInvalidaException("A devolução prevista deve ser depois da retirada.");

        Cliente cliente = banco.clientes.buscar(cpf.replaceAll("\\D", ""));
        Veiculo veiculo = banco.veiculos.buscar(placa.trim().toUpperCase());
        veiculo.alugar(); // lança VeiculoIndisponivelException se ocupado/manutenção

        Contrato contrato = new Contrato(String.format("C%04d", ++banco.sequenciaContrato), cliente, veiculo, retirada, prevista);
        banco.contratos.adicionar(contrato);
        persistencia.salvar(banco);
        persistencia.salvarComprovante(contrato.getId() + "-contrato", contrato.gerarTexto());
        return contrato;
    }

    public Contrato devolver(String idContrato, LocalDate dataDevolucao, double kmFinal)
            throws RotaSeguraException, IOException {
        Contrato contrato = banco.contratos.buscar(idContrato.trim().toUpperCase());
        contrato.encerrar(dataDevolucao, kmFinal);
        persistencia.salvar(banco);
        persistencia.salvarComprovante(contrato.getId() + "-comprovante", contrato.gerarTexto());
        return contrato;
    }

    /** Disponibilidade em tempo real: apenas veículos livres neste instante. */
    public List<Veiculo> listarDisponiveis() {
        return banco.veiculos.filtrar(v -> v.getStatus() == StatusVeiculo.DISPONIVEL);
    }

    /** Histórico completo de locações (abertas e encerradas) de um cliente. */
    public List<Contrato> historicoPorCliente(String cpf) throws RotaSeguraException {
        Cliente cliente = banco.clientes.buscar(cpf.replaceAll("\\D", ""));
        return banco.contratos.filtrar(c -> c.getCliente().getId().equals(cliente.getId()));
    }

    /** Histórico completo de locações de um veículo. */
    public List<Contrato> historicoPorVeiculo(String placa) throws RotaSeguraException {
        Veiculo veiculo = banco.veiculos.buscar(placa.trim().toUpperCase());
        return banco.contratos.filtrar(c -> c.getVeiculo().getId().equals(veiculo.getId()));
    }

    public List<Veiculo> listarVeiculos() { return banco.veiculos.listar(); }
    public List<Cliente> listarClientes() { return banco.clientes.listar(); }
    public List<Contrato> listarContratos() { return banco.contratos.listar(); }
}

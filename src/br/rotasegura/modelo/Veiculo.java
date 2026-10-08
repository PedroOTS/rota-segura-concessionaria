package br.rotasegura.modelo;

import br.rotasegura.excecao.DadosInvalidosException;
import br.rotasegura.excecao.VeiculoIndisponivelException;
import java.io.Serializable;
import java.time.Year;

/**
 * ABSTRAÇÃO + ENCAPSULAMENTO: classe-base de todos os veículos.
 * Os atributos são privados; só mudam por métodos que validam.
 * Os cálculos de preço são abstratos (POLIMORFISMO nas subclasses).
 */
public abstract class Veiculo implements Identificavel, Serializable {
    private static final long serialVersionUID = 1L;

    private final String placa;
    private String modelo;
    private int ano;
    private double quilometragem;
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;

    protected Veiculo(String placa, String modelo, int ano) throws DadosInvalidosException {
        if (placa == null || !placa.trim().toUpperCase().matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new DadosInvalidosException("Placa inválida (use AAA1234 ou AAA1B23): " + placa);
        }
        this.placa = placa.trim().toUpperCase();
        setModelo(modelo);
        setAno(ano);
    }

    // ----- comportamento polimórfico -----
    public abstract String getCategoria();
    public abstract double calcularDiaria();
    public abstract double calcularSeguro(int dias);
    public abstract double calcularManutencao(int dias);

    // ----- getters -----
    @Override public String getId() { return placa; }
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public int getAno() { return ano; }
    public double getQuilometragem() { return quilometragem; }
    public StatusVeiculo getStatus() { return status; }

    // ----- setters com validação -----
    public final void setModelo(String modelo) throws DadosInvalidosException {
        if (modelo == null || modelo.trim().length() < 2) {
            throw new DadosInvalidosException("Modelo inválido.");
        }
        this.modelo = modelo.trim();
    }

    public final void setAno(int ano) throws DadosInvalidosException {
        int max = Year.now().getValue() + 1;
        if (ano < 1990 || ano > max) {
            throw new DadosInvalidosException("Ano deve estar entre 1990 e " + max + ".");
        }
        this.ano = ano;
    }

    public void setQuilometragem(double km) throws DadosInvalidosException {
        if (km < quilometragem) {
            throw new DadosInvalidosException(
                "Quilometragem informada (" + km + ") menor que a atual (" + quilometragem + ").");
        }
        this.quilometragem = km;
    }

    // ----- transições de estado controladas -----
    public void alugar() throws VeiculoIndisponivelException {
        if (status != StatusVeiculo.DISPONIVEL) {
            throw new VeiculoIndisponivelException(
                "Veículo " + placa + " não está disponível (status: " + status + ").");
        }
        status = StatusVeiculo.LOCADO;
    }

    public void liberar() { status = StatusVeiculo.DISPONIVEL; }

    public void enviarParaManutencao() throws VeiculoIndisponivelException {
        if (status == StatusVeiculo.LOCADO) {
            throw new VeiculoIndisponivelException("Veículo " + placa + " está locado.");
        }
        status = StatusVeiculo.MANUTENCAO;
    }

    @Override public String toString() {
        return String.format("%-8s %-8s %-18s %d  %.0f km  [%s]  diária R$ %.2f",
            getCategoria(), placa, modelo, ano, quilometragem, status, calcularDiaria());
    }
}

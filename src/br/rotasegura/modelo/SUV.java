package br.rotasegura.modelo;

import br.rotasegura.excecao.DadosInvalidosException;

/** HERANÇA: especialização de Veiculo com regras próprias de preço. */
public class SUV extends Veiculo {
    private static final long serialVersionUID = 1L;
    private static final double DIARIA = 250.0;
    private static final double PERCENTUAL_SEGURO = 0.12;   // sobre o valor das diárias
    private static final double MANUTENCAO_POR_DIA = 20.0;

    public SUV(String placa, String modelo, int ano) throws DadosInvalidosException {
        super(placa, modelo, ano);
    }

    @Override public String getCategoria() { return "SUV"; }
    @Override public double calcularDiaria() { return DIARIA; }
    @Override public double calcularSeguro(int dias) { return DIARIA * dias * PERCENTUAL_SEGURO; }
    @Override public double calcularManutencao(int dias) { return MANUTENCAO_POR_DIA * dias; }
}

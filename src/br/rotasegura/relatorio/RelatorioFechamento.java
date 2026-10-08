package br.rotasegura.relatorio;

import br.rotasegura.modelo.Contrato;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Relatório de fechamento: faturamento total e por categoria de veículo. */
public class RelatorioFechamento implements Imprimivel {
    private final List<Contrato> contratos;

    public RelatorioFechamento(List<Contrato> contratos) {
        this.contratos = contratos;
    }

    @Override
    public String gerarTexto() {
        Map<String, Double> porCategoria = new TreeMap<>();
        int abertos = 0, encerrados = 0;
        double total = 0;
        for (Contrato c : contratos) {
            if (c.getStatus() == Contrato.Status.ENCERRADO) {
                encerrados++;
                total += c.getTotal();
                porCategoria.merge(c.getVeiculo().getCategoria(), c.getTotal(), Double::sum);
            } else {
                abertos++;
            }
        }
        StringBuilder sb = new StringBuilder("====== RELATÓRIO DE FECHAMENTO ======\n");
        sb.append("Contratos encerrados: ").append(encerrados).append("\n");
        sb.append("Contratos em aberto : ").append(abertos).append("\n");
        porCategoria.forEach((cat, v) -> sb.append(String.format("  %-8s R$ %10.2f%n", cat, v)));
        sb.append(String.format("FATURAMENTO TOTAL   : R$ %.2f%n", total));
        return sb.toString();
    }
}

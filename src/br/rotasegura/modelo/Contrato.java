package br.rotasegura.modelo;

import br.rotasegura.excecao.DadosInvalidosException;
import br.rotasegura.excecao.DataInvalidaException;
import br.rotasegura.relatorio.Imprimivel;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Contrato de locação. Implementa Imprimivel (interface padronizada de documentos). */
public class Contrato implements Identificavel, Imprimivel, Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double MULTA_ATRASO = 0.20; // 20% da diária por dia excedente

    public enum Status { ABERTO, ENCERRADO }

    private final String id;
    private final Cliente cliente;
    private final Veiculo veiculo;
    private final LocalDate retirada;
    private final LocalDate devolucaoPrevista;
    private LocalDate devolucaoReal;
    private Status status = Status.ABERTO;

    private int diasCobrados;
    private double valorDiarias, valorSeguro, valorManutencao, multa, total;

    public Contrato(String id, Cliente cliente, Veiculo veiculo, LocalDate retirada, LocalDate prevista) {
        this.id = id;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.retirada = retirada;
        this.devolucaoPrevista = prevista;
    }

    /** Fecha o contrato: valida datas/km e calcula valores usando o polimorfismo do veículo. */
    public void encerrar(LocalDate dataReal, double kmFinal) throws DataInvalidaException, DadosInvalidosException {
        if (status == Status.ENCERRADO) throw new DadosInvalidosException("Contrato " + id + " já foi encerrado.");
        if (dataReal == null || dataReal.isBefore(retirada)) {
            throw new DataInvalidaException("Data de devolução anterior à data de retirada (" + retirada.format(FMT) + ").");
        }
        veiculo.setQuilometragem(kmFinal); // lança exceção se km inválida (antes de qualquer alteração)

        int previstos = (int) Math.max(1, ChronoUnit.DAYS.between(retirada, devolucaoPrevista));
        diasCobrados = (int) Math.max(1, ChronoUnit.DAYS.between(retirada, dataReal));
        int extras = Math.max(0, diasCobrados - previstos);

        valorDiarias = veiculo.calcularDiaria() * diasCobrados;
        valorSeguro = veiculo.calcularSeguro(diasCobrados);
        valorManutencao = veiculo.calcularManutencao(diasCobrados);
        multa = extras * veiculo.calcularDiaria() * MULTA_ATRASO;
        total = valorDiarias + valorSeguro + valorManutencao + multa;

        devolucaoReal = dataReal;
        status = Status.ENCERRADO;
        veiculo.liberar();
    }

    @Override public String getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Veiculo getVeiculo() { return veiculo; }
    public Status getStatus() { return status; }
    public double getTotal() { return total; }

    @Override
    public String gerarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("   ROTA SEGURA - ").append(status == Status.ABERTO ? "CONTRATO DE LOCAÇÃO" : "COMPROVANTE DE DEVOLUÇÃO").append("\n");
        sb.append("==========================================\n");
        sb.append("Contrato : ").append(id).append("  (").append(status).append(")\n");
        sb.append("Cliente  : ").append(cliente.getNome()).append(" - CPF ").append(cliente.getCpf()).append("\n");
        sb.append("Veículo  : ").append(veiculo.getCategoria()).append(" ").append(veiculo.getModelo())
          .append(" (").append(veiculo.getPlaca()).append(")\n");
        sb.append("Retirada : ").append(retirada.format(FMT)).append("\n");
        sb.append("Prevista : ").append(devolucaoPrevista.format(FMT)).append("\n");
        if (status == Status.ENCERRADO) {
            sb.append("Devolução: ").append(devolucaoReal.format(FMT)).append("  (").append(diasCobrados).append(" dia(s) cobrados)\n");
            sb.append(String.format("Diárias    : R$ %9.2f%n", valorDiarias));
            sb.append(String.format("Seguro     : R$ %9.2f%n", valorSeguro));
            sb.append(String.format("Manutenção : R$ %9.2f%n", valorManutencao));
            sb.append(String.format("Multa      : R$ %9.2f%n", multa));
            sb.append(String.format("TOTAL      : R$ %9.2f%n", total));
        } else {
            sb.append(String.format("Diária: R$ %.2f%n", veiculo.calcularDiaria()));
        }
        return sb.toString();
    }

    @Override public String toString() {
        return String.format("%s | %s | %s | %s | %s", id, status, cliente.getNome(), veiculo.getPlaca(), retirada.format(FMT));
    }
}

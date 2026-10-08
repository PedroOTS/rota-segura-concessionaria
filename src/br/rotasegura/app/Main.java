package br.rotasegura.app;

import br.rotasegura.excecao.RotaSeguraException;
import br.rotasegura.modelo.*;
import br.rotasegura.persistencia.ArquivoPersistencia;
import br.rotasegura.relatorio.Imprimivel;
import br.rotasegura.relatorio.RelatorioFechamento;
import br.rotasegura.servico.LocadoraService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private static final Scanner in = new Scanner(System.in);
    private static LocadoraService service;

    @FunctionalInterface
    private interface Acao { void executar() throws Exception; }

    public static void main(String[] args) throws Exception {
        service = new LocadoraService(new ArquivoPersistencia("dados/rotasegura.dat", "dados/comprovantes"));
        int op;
        do {
            System.out.println("\n===== ROTA SEGURA =====");
            System.out.println("1) Cadastrar veículo      2) Listar veículos");
            System.out.println("3) Cadastrar cliente      4) Listar clientes");
            System.out.println("5) Abrir locação          6) Devolver veículo");
            System.out.println("7) Listar contratos       8) Relatório de fechamento");
            System.out.println("9) Demonstração de erros  10) Veículos disponíveis");
            System.out.println("11) Histórico do cliente  12) Histórico do veículo");
            System.out.println("0) Sair");
            System.out.print("Opção: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> executar(Main::cadastrarVeiculo);
                case 2 -> imprimirLista(service.listarVeiculos());
                case 3 -> executar(Main::cadastrarCliente);
                case 4 -> imprimirLista(service.listarClientes());
                case 5 -> executar(Main::abrirLocacao);
                case 6 -> executar(Main::devolver);
                case 7 -> imprimirLista(service.listarContratos());
                case 8 -> new RelatorioFechamento(service.listarContratos()).imprimir();
                case 9 -> demonstracao();
                case 10 -> imprimirLista(service.listarDisponiveis());
                case 11 -> executar(() -> imprimirLista(service.historicoPorCliente(ler("CPF do cliente: "))));
                case 12 -> executar(() -> imprimirLista(service.historicoPorVeiculo(ler("Placa: "))));
                case 0 -> System.out.println("Até logo!");
                default -> System.out.println("Opção inválida.");
            }
        } while (op != 0);
    }

    // ---------- fluxos de entrada ----------
    private static void cadastrarVeiculo() throws Exception {
        String tipo = ler("Categoria (1-Popular, 2-Sedan, 3-SUV): ");
        String placa = ler("Placa: ");
        String modelo = ler("Modelo: ");
        int ano = Integer.parseInt(ler("Ano: "));
        Veiculo v = switch (tipo) {
            case "1" -> new Popular(placa, modelo, ano);
            case "2" -> new Sedan(placa, modelo, ano);
            case "3" -> new SUV(placa, modelo, ano);
            default -> throw new IllegalArgumentException("Categoria inválida.");
        };
        service.cadastrarVeiculo(v);
        System.out.println("Veículo cadastrado: " + v);
    }

    private static void cadastrarCliente() throws Exception {
        Cliente c = new Cliente(ler("CPF: "), ler("Nome: "), ler("Telefone (com DDD): "), ler("E-mail: "));
        service.cadastrarCliente(c);
        System.out.println("Cliente cadastrado: " + c);
    }

    private static void abrirLocacao() throws Exception {
        Contrato c = service.abrirLocacao(ler("CPF do cliente: "), ler("Placa: "),
                LocalDate.parse(ler("Retirada (dd/MM/aaaa): "), FMT),
                LocalDate.parse(ler("Devolução prevista (dd/MM/aaaa): "), FMT));
        c.imprimir();
    }

    private static void devolver() throws Exception {
        Contrato c = service.devolver(ler("Número do contrato: "),
                LocalDate.parse(ler("Data da devolução (dd/MM/aaaa): "), FMT),
                Double.parseDouble(ler("Quilometragem final: ").replace(',', '.')));
        c.imprimir();
    }

    // ---------- tratamento de erros da interface ----------
    private static void executar(Acao acao) {
        try {
            acao.executar();
        } catch (RotaSeguraException e) {
            System.out.println("[ERRO DE NEGÓCIO] " + e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("[ERRO] Data inválida. Use dd/MM/aaaa e uma data real.");
        } catch (NumberFormatException e) {
            System.out.println("[ERRO] Número inválido.");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERRO] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERRO INESPERADO] " + e.getMessage());
        }
    }

    // GENERICS: método genérico que imprime qualquer lista
    private static <T> void imprimirLista(List<T> itens) {
        if (itens.isEmpty()) System.out.println("(nenhum registro)");
        itens.forEach(System.out::println);
    }

    // ---------- demonstração para a apresentação ----------
    private static void demonstracao() {
        System.out.println("\n--- DEMONSTRAÇÃO: fluxo completo + erros tratados ---");
        LocalDate hoje = LocalDate.now();
        executar(() -> service.cadastrarVeiculo(new Popular("DEM1A23", "Gol", 2022)));
        executar(() -> service.cadastrarCliente(new Cliente("529.982.247-25", "Cliente Demo", "(14) 99999-0000", "demo@email.com")));

        System.out.println("\n1) Placa inválida:");
        executar(() -> service.cadastrarVeiculo(new Sedan("123", "Civic", 2021)));
        System.out.println("\n2) CPF inválido:");
        executar(() -> service.cadastrarCliente(new Cliente("111.111.111-11", "Fulano", "14999990000", "f@x.com")));
        System.out.println("\n3) Datas inconsistentes (devolução antes da retirada):");
        executar(() -> service.abrirLocacao("52998224725", "DEM1A23", hoje, hoje.minusDays(2)));

        System.out.println("\n4) Locação válida:");
        final Contrato[] c = new Contrato[1];
        executar(() -> { c[0] = service.abrirLocacao("52998224725", "DEM1A23", hoje, hoje.plusDays(3)); c[0].imprimir(); });

        System.out.println("\n5) Tentar alugar veículo já ocupado:");
        executar(() -> service.abrirLocacao("52998224725", "DEM1A23", hoje, hoje.plusDays(2)));

        if (c[0] != null) {
            System.out.println("\n6) Devolução antes da retirada:");
            executar(() -> service.devolver(c[0].getId(), hoje.minusDays(1), 1000));
            System.out.println("\n7) Devolução com 5 dias (2 de atraso) e comprovante:");
            executar(() -> service.devolver(c[0].getId(), hoje.plusDays(5), 1000).imprimir());
        }
    }

    // ---------- utilitários ----------
    private static String ler(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private static int lerOpcao() {
        try { return Integer.parseInt(in.nextLine().trim()); } catch (NumberFormatException e) { return -1; }
    }
}

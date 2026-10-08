package br.rotasegura.relatorio;

/** POLIMORFISMO: interface padronizada para contratos, comprovantes e relatórios. */
public interface Imprimivel {
    String gerarTexto();

    default void imprimir() {
        System.out.println(gerarTexto());
    }
}

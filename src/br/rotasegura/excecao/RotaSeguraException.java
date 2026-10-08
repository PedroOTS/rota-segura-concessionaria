package br.rotasegura.excecao;

/** Exceção-base (checked) de todas as regras de negócio do sistema. */
public class RotaSeguraException extends Exception {
    public RotaSeguraException(String mensagem) { super(mensagem); }
}

package br.rotasegura.modelo;

import br.rotasegura.excecao.DadosInvalidosException;
import java.io.Serializable;

/** ENCAPSULAMENTO: dados cadastrais protegidos e validados. */
public class Cliente implements Identificavel, Serializable {
    private static final long serialVersionUID = 1L;

    private final String cpf;   // somente dígitos
    private String nome;
    private String telefone;
    private String email;

    public Cliente(String cpf, String nome, String telefone, String email) throws DadosInvalidosException {
        if (!cpfValido(cpf)) throw new DadosInvalidosException("CPF inválido: " + cpf);
        this.cpf = cpf.replaceAll("\\D", "");
        setNome(nome);
        setTelefone(telefone);
        setEmail(email);
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null) return false;
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11 || d.chars().distinct().count() == 1) return false;
        for (int t = 9; t < 11; t++) {
            int soma = 0;
            for (int i = 0; i < t; i++) soma += (d.charAt(i) - '0') * (t + 1 - i);
            int dv = (soma * 10) % 11;
            if (dv == 10) dv = 0;
            if (dv != d.charAt(t) - '0') return false;
        }
        return true;
    }

    @Override public String getId() { return cpf; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.trim().length() < 3) throw new DadosInvalidosException("Nome inválido.");
        this.nome = nome.trim();
    }

    public void setTelefone(String telefone) throws DadosInvalidosException {
        String d = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (d.length() < 10 || d.length() > 11) throw new DadosInvalidosException("Telefone inválido (use DDD + número).");
        this.telefone = d;
    }

    public void setEmail(String email) throws DadosInvalidosException {
        if (email == null || !email.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) {
            throw new DadosInvalidosException("E-mail inválido.");
        }
        this.email = email.trim();
    }

    @Override public String toString() {
        return String.format("%s  CPF %s  %s  %s", nome, cpf, telefone, email);
    }
}

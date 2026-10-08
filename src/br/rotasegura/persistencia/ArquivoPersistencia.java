package br.rotasegura.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Persistência: banco binário (serialização) + comprovantes em arquivos .txt. */
public class ArquivoPersistencia {
    private final Path arquivo;
    private final Path pastaComprovantes;

    public ArquivoPersistencia(String arquivo, String pastaComprovantes) {
        this.arquivo = Paths.get(arquivo);
        this.pastaComprovantes = Paths.get(pastaComprovantes);
    }

    public void salvar(BancoDeDados banco) throws IOException {
        if (arquivo.getParent() != null) {
            Files.createDirectories(arquivo.getParent()); // cria a pasta dados/ na primeira execução
        }
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(arquivo))) {
            out.writeObject(banco);
        }
    }

    public BancoDeDados carregar() throws IOException, ClassNotFoundException {
        if (!Files.exists(arquivo)) return new BancoDeDados();
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            return (BancoDeDados) in.readObject();
        }
    }

    public Path salvarComprovante(String nome, String texto) throws IOException {
        Files.createDirectories(pastaComprovantes);
        Path destino = pastaComprovantes.resolve(nome + ".txt");
        Files.write(destino, texto.getBytes(StandardCharsets.UTF_8));
        return destino;
    }
}

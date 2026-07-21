package br.com.isiflix.isicode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Workspace {

    private final Path root;

    public Workspace(Path root) {
        this.root = root.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível criar ou acessar o workspace: " + this.root, e);
        }
    }

    public Path resolve(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("O caminho não pode ser vazio.");
        }

        Path supplied = Path.of(relativePath);
        if (supplied.isAbsolute()) {
            throw new SecurityException("Caminhos absolutos não são permitidos.");
        }

        Path resolved = root.resolve(supplied).normalize();
        if (!resolved.startsWith(root)) {
            throw new SecurityException("Acesso fora do workspace bloqueado.");
        }
        return resolved;
    }

    public Path root() {
        return root;
    }
}

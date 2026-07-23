package br.com.isiflix.isicode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PromptLoader {

    private final Path promptsDirectory;

    public PromptLoader() {
        this(resolvePromptsDirectory());
    }

    public PromptLoader(Path promptsDirectory) {
        if (promptsDirectory == null) {
            throw new IllegalArgumentException(
                    "A pasta de prompts é obrigatória"
            );
        }

        this.promptsDirectory = promptsDirectory
                .toAbsolutePath()
                .normalize();
    }
    
    public String loadCombinedPrompt() {
        return load("/Users/isidro/.isicode/prompts/system.md")
                + System.lineSeparator()
                + System.lineSeparator()
                + load("/Users/isidro/.isicode/prompts/react-vite.md")
                + System.lineSeparator()
                + System.lineSeparator()
                + load("/Users/isidro/.isicode/prompts/design-system.md");
    }

    public  String load(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do prompt é obrigatório"
            );
        }

        Path promptFile = promptsDirectory
                .resolve(filename)
                .normalize();

        if (!promptFile.startsWith(promptsDirectory)) {
            throw new SecurityException(
                    "O arquivo está fora da pasta de prompts: "
                            + filename
            );
        }

        if (!Files.isRegularFile(promptFile)) {
            throw new IllegalStateException(
                    "Prompt não encontrado: " + promptFile
            );
        }

        try {
            return Files.readString(
                    promptFile,
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível ler o prompt: " + promptFile,
                    exception
            );
        }
    }

    public Path getPromptsDirectory() {
        return promptsDirectory;
    }

    private static Path resolvePromptsDirectory() {
        String configuredDirectory =
                System.getenv("ISICODE_PROMPTS_DIR");

        if (configuredDirectory != null &&
                !configuredDirectory.isBlank()) {

            return Paths.get(configuredDirectory);
        }

        return Paths.get(
                System.getProperty("user.home"),
                ".isicode",
                "prompts"
        );
    }
}
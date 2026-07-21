package br.com.isiflix.isicode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class PromptLoader {

    private PromptLoader() {
    }

    public static String loadCombinedPrompt() {
        return load("prompts/system.md")
                + System.lineSeparator()
                + System.lineSeparator()
                + load("prompts/react-vite.md");
    }

    private static String load(String resource) {
        try (InputStream input = PromptLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Prompt não encontrado no classpath: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar o prompt: " + resource, e);
        }
    }
}

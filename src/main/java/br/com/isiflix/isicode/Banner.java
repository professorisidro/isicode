package br.com.isiflix.isicode;

import java.nio.file.Path;

public final class Banner {

    private Banner() {
    }

    public static void show(Path workspace, String model) {
        System.out.println("""
               ____      _ ______ ____  ____  ______
              /  _/____ (_)/ ____// __ \\/ __ \\/ ____/
              / / / __ \\/ / /    / / / / / / / __/
            _/ / / /_/ / / /___ / /_/ / /_/ / /___
           /___/ \\____/_/\\____/ \\____/_____/_____/

           IsiCODE — Local AI Software Engineer
           Powered by LangChain4j + Ollama
           """);
        System.out.println("Modelo    : " + model);
        System.out.println("Workspace : " + workspace.toAbsolutePath().normalize());
        System.out.println("Comandos  : /help  /clear  /exit");
        System.out.println("────────────────────────────────────────────────────────────");
    }
}

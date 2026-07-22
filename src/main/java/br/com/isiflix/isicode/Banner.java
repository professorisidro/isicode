package br.com.isiflix.isicode;

import java.nio.file.Path;

public final class Banner {

    private Banner() {
    }

    public static void show(Path workspace, String model) {
        System.out.println("""
  __   _     _   __    ____          _         		      
 | _| (_)___(_) |_ |  / ___|___   __| | ___ 
 | |  | / __| |  | | | |   / _ \\ / _` |/ _ \\
 | |  | \\__ \\ |  | | | |__| (_) | (_| |  __/
 | |  |_|___/_|  | |  \\____\\___/ \\__,_|\\___|
 |__|           |__|                        
           IsiCODE — Local AI Software Engineer
           Powered by LangChain4j + Ollama
           """);
        System.out.println("Modelo    : " + model);
        System.out.println("Workspace : " + workspace.toAbsolutePath().normalize());
        System.out.println("Comandos  : /help  /clear  /exit");
        System.out.println("────────────────────────────────────────────────────────────");
    }
}

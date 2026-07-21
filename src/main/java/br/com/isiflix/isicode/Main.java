package br.com.isiflix.isicode;

import java.nio.file.Path;
import java.util.Scanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path workspacePath = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        Workspace workspace = new Workspace(workspacePath);
        String systemPrompt = PromptLoader.loadCombinedPrompt();
        ToolExecutor tools = new ToolExecutor(workspace);
        IsiCodeAssistant assistant = AssistantFactory.create(tools, systemPrompt);

        Banner.show(workspace.root(), AssistantFactory.configuredModel());
        showSecondTerminalHint(workspace.root());

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(System.lineSeparator() + "PROMPT> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();
                if (input.isBlank()) {
                    continue;
                }

                switch (input.toLowerCase()) {
                    case "/exit", "/quit" -> {
                        System.out.println("IsiCODE encerrado.");
                        return;
                    }
                    case "/help" -> {
                        showHelp(workspace.root());
                        continue;
                    }
                    case "/clear" -> {
                        assistant = AssistantFactory.create(tools, systemPrompt);
                        System.out.println("✓ Histórico da conversa limpo. Os arquivos foram preservados.");
                        continue;
                    }
                    default -> {
                        try {
                            System.out.println("IsiCODE > Analisando sua solicitação...");
                            String response = assistant.chat(input);
                            System.out.println(System.lineSeparator() + "IsiCODE > " + response.strip());
                        } catch (Exception e) {
                            System.out.println("IsiCODE > Não foi possível concluir a solicitação.");
                            System.out.println("Detalhe: " + rootMessage(e));
                            System.out.println("Verifique se o Ollama está em execução e se o modelo está instalado.");
                        }
                    }
                }
            }
        }
    }

    private static void showSecondTerminalHint(Path workspace) {
        System.out.println();
        System.out.println("Para visualizar a aplicação em outro terminal:");
        System.out.println("  cd \"" + workspace.toAbsolutePath().normalize() + "\"");
        System.out.println("  npm run dev");
        System.out.println();
        System.out.println("O IsiCODE não inicia o servidor Vite para manter este terminal conversacional.");
    }

    private static void showHelp(Path workspace) {
        System.out.println("""
                /help   Exibe esta ajuda
                /clear  Limpa a memória da conversa sem alterar arquivos
                /exit   Encerra o IsiCODE

                Exemplos:
                  Crie uma landing page para um evento de inteligência artificial.
                  Adicione um formulário com nome, email e WhatsApp.
                  Corrija os erros atuais de build.
                  Transforme o layout em modo escuro.
                """);
        showSecondTerminalHint(workspace);
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}

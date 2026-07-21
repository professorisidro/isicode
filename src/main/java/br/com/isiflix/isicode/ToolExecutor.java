package br.com.isiflix.isicode;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class ToolExecutor {

	private static final long MAX_FILE_SIZE = 300_000;
	private static final int MAX_OUTPUT_CHARS = 20_000;
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final Set<String> IGNORED_DIRECTORIES = Set.of("node_modules", ".git", "dist", ".idea", ".vscode",
			"target");

	private final Workspace workspace;

	public ToolExecutor(Workspace workspace) {
		this.workspace = workspace;
	}

	@Tool("Lista os arquivos e diretórios existentes dentro de um caminho do workspace. Use antes de decidir quais arquivos ler ou criar.")
	public String listFiles(
			@P(name = "path", value = "Caminho relativo do diretório. Use '.' para a raiz do workspace.") String path) {
		status("Inspecionando", path);
		Path directory = workspace.resolve(path);

		if (!Files.exists(directory)) {
			return "Diretório inexistente: " + path;
		}
		if (!Files.isDirectory(directory)) {
			return "O caminho não é um diretório: " + path;
		}

		try (Stream<Path> stream = Files.walk(directory, 4)) {
			String result = stream.filter(item -> !item.equals(directory)).filter(this::isVisible)
					.sorted(Comparator.comparing(Path::toString)).map(item -> {
						String relative = workspace.root().relativize(item).toString();
						return Files.isDirectory(item) ? relative + "/" : relative;
					}).limit(500).reduce((a, b) -> a + System.lineSeparator() + b).orElse("(diretório vazio)");
			success("Estrutura carregada");
			return result;
		} catch (IOException e) {
			return "Erro ao listar diretório: " + e.getMessage();
		}
	}

	@Tool("Lê o conteúdo completo de um arquivo textual do workspace. Use antes de modificar um arquivo existente.")
	public String readFile(@P(name = "path", value = "Caminho relativo do arquivo dentro do workspace.") String path) {
		status("Lendo arquivo", path);
		Path file = workspace.resolve(path);

		if (!Files.exists(file)) {
			return "Arquivo inexistente: " + path;
		}
		if (!Files.isRegularFile(file)) {
			return "O caminho não é um arquivo: " + path;
		}

		try {
			long size = Files.size(file);
			if (size > MAX_FILE_SIZE) {
				return "Arquivo grande demais para leitura: " + size + " bytes.";
			}
			String content = Files.readString(file, StandardCharsets.UTF_8);
			success("Arquivo carregado: " + path);
			return content;
		} catch (IOException e) {
			return "Erro ao ler arquivo: " + e.getMessage();
		}
	}

	@Tool("Cria ou substitui um arquivo dentro do workspace. O conteúdo não é exibido no terminal; apenas o status da gravação.")
	public String writeFile(@P(name = "path", value = "Caminho relativo do arquivo a criar ou atualizar.") String path,
			@P(name = "content", value = "Conteúdo completo que será gravado no arquivo.") String content) {
		Path file = workspace.resolve(path);
		boolean existed = Files.exists(file);
		status(existed ? "Atualizando arquivo" : "Gerando arquivo", path);

		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			success((existed ? "Atualizado: " : "Criado: ") + path);
			return "Arquivo salvo com sucesso: " + path;
		} catch (IOException e) {
			error("Falha ao salvar: " + path);
			return "Erro ao escrever arquivo: " + e.getMessage();
		}
	}

	@Tool("Executa um comando curto e seguro no workspace, como npm install, npm run build, npm run lint ou npx tsc --noEmit. Não use para iniciar servidores persistentes.")
	public String runCommand(
			@P(name="command", value = "Comando e argumentos em uma lista, por exemplo ['npm','run','build'].") List<String> command,
			@P(name="timeoutSeconds", value="Timeout em segundos. Use entre 10 e 300.") int timeoutSeconds) {
		if (command == null || command.isEmpty()) {
			return "Comando vazio.";
		}
		if (!isAllowed(command)) {
			error("Comando bloqueado: " + String.join(" ", command));
			return "Comando bloqueado pela política de segurança: " + String.join(" ", command);
		}

		int timeout = Math.max(10, Math.min(timeoutSeconds, 300));
		status("Executando", String.join(" ", command));

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(workspace.root().toFile());
		builder.redirectErrorStream(true);

		try {
			Process process = builder.start();
			StringBuilder output = new StringBuilder();

			Thread reader = Thread.startVirtualThread(() -> readOutput(process, output));
			boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);

			if (!finished) {
				process.destroyForcibly();
				reader.join(Duration.ofSeconds(2));
				error("Timeout após " + timeout + " segundos");
				return "Comando interrompido por timeout.";
			}

			reader.join(Duration.ofSeconds(2));
			int exitCode = process.exitValue();
			if (exitCode == 0) {
				success("Comando concluído");
			} else {
				error("Comando terminou com código " + exitCode);
			}

			return "exitCode=" + exitCode + System.lineSeparator() + trimOutput(output.toString());
		} catch (IOException e) {
			error("Não foi possível iniciar o comando");
			return "Erro ao executar comando: " + e.getMessage();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return "Execução interrompida.";
		}
	}

	private boolean isAllowed(List<String> command) {
		List<String> normalized = command.stream().map(String::toLowerCase).toList();
		String joined = String.join(" ", normalized);

		if (joined.contains("npm run dev") || joined.contains("npx vite") || joined.contains("--host")) {
			return false;
		}

		if (normalized.getFirst().equals("npm")) {
			return normalized.size() >= 2 && (normalized.get(1).equals("install") || normalized.get(1).equals("i")
					|| (normalized.get(1).equals("run") && normalized.size() >= 3
							&& Set.of("build", "lint", "test", "typecheck").contains(normalized.get(2)))
					|| (normalized.get(1).equals("create") && joined.contains("vite")));
		}

		return normalized.getFirst().equals("npx") && normalized.size() >= 2
				&& Set.of("tsc", "eslint", "vitest").contains(normalized.get(1));
	}

	private boolean isVisible(Path path) {
		for (Path part : workspace.root().relativize(path)) {
			if (IGNORED_DIRECTORIES.contains(part.toString())) {
				return false;
			}
		}
		return true;
	}

	private static void readOutput(Process process, StringBuilder output) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (output.length() < MAX_OUTPUT_CHARS) {
					output.append(line).append(System.lineSeparator());
				}
			}
		} catch (IOException ignored) {
		}
	}

	private static String trimOutput(String output) {
		if (output.length() <= MAX_OUTPUT_CHARS) {
			return output;
		}
		return output.substring(0, MAX_OUTPUT_CHARS) + System.lineSeparator() + "[saída truncada]";
	}

	private static void status(String action, String target) {
		System.out.printf("[%s] • %s: %s%n", LocalTime.now().format(TIME), action, target);
	}

	private static void success(String message) {
		System.out.printf("[%s] ✓ %s%n", LocalTime.now().format(TIME), message);
	}

	private static void error(String message) {
		System.out.printf("[%s] ✗ %s%n", LocalTime.now().format(TIME), message);
	}
}

package br.com.isiflix.isicode;

import java.time.Duration;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public final class AssistantFactory {
	
	static String baseUrl;
    static String modelName;

	private AssistantFactory() {
	}

	public static IsiCodeAssistant create(ToolExecutor tools, String systemPrompt, String mode) {
		baseUrl = env("ISICODE_OLLAMA_URL", "http://localhost:11434");
		modelName = env("ISICODE_MODEL", "qwen3:8b");

		ChatModel model;
		if (mode.equals("--stac")) {
			model = OpenAiChatModel.builder().baseUrl("https://llmserve-docker.up.railway.app/v1")
					.apiKey("177a2f1b5fde04076bba8776e8c4bd80be36d18d0e4b2e80901982e3b82ec143")
					.modelName("vibecoder-base").temperature(0.1).logRequests(true).logResponses(true).build();

		} else {

			model = OllamaChatModel.builder().baseUrl(baseUrl).modelName(modelName).temperature(0.1)
					.timeout(Duration.ofMinutes(5)).build();
		}

		

		return AiServices.builder(IsiCodeAssistant.class).chatModel(model)
				.chatMemory(MessageWindowChatMemory.withMaxMessages(80)).systemMessageProvider(memoryId -> systemPrompt)
				.tools(tools).build();
	}

	public static String configuredModel() {
		return modelName;
	}

	private static String env(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}
}

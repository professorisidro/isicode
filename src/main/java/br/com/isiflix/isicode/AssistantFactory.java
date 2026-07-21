package br.com.isiflix.isicode;

import java.time.Duration;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

public final class AssistantFactory {

    private AssistantFactory() {
    }

    public static IsiCodeAssistant create(ToolExecutor tools, String systemPrompt) {
        String baseUrl = env("ISICODE_OLLAMA_URL", "http://localhost:11434");
        String modelName = env("ISICODE_MODEL", "qwen3:8b");

        ChatModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(5))
                .build();

        return AiServices.builder(IsiCodeAssistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(80))
                .systemMessageProvider(memoryId -> systemPrompt)
                .tools(tools)
                .build();
    }

    public static String configuredModel() {
        return env("ISICODE_MODEL", "qwen3:8b");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}

package br.com.isiflix.isicode;

import dev.langchain4j.service.UserMessage;

public interface IsiCodeAssistant {

    String chat(@UserMessage String message);
}

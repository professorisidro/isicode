# IsiCODE

Agente local de desenvolvimento React usando Java 21, LangChain4j 1.18.0 e Ollama.

## Requisitos

- Java 21
- Maven 3.9+
- Node.js e npm
- Ollama
- Um modelo com suporte a tool calling


## Modelos que aceitam Tool Calling e que funcionaram localmente

- qwen3:8b
- granite-4.1:3b


## Modelo padrão

```bash
ollama pull qwen2.5-coder:7b
```

Para trocar o modelo:

```bash
export ISICODE_MODEL=qwen3-coder:30b
```

Para trocar a URL do Ollama:

```bash
export ISICODE_OLLAMA_URL=http://localhost:11434
```

## Compilar

```bash
mvn clean package
```

## Executar

```bash
java -jar target/isicode.jar ./meu-projeto-react
```

## Segundo terminal

Depois que a aplicação React existir:

```bash
cd ./meu-projeto-react
npm run dev
```

O servidor Vite fica no segundo terminal, enquanto o primeiro permanece dedicado à conversa com o IsiCODE.

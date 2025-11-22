package cz.cuni.mff.hanaf.mainapp.llm;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaPropertyMapper {

    private final LlmProperties llmProperties;

    public OllamaPropertyMapper(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    @PostConstruct
    public void mapProperties() {
        System.setProperty("spring.ai.ollama.base-url", llmProperties.getBaseUrl());
        System.setProperty("spring.ai.ollama.chat.options.model", llmProperties.getChat().getModel());
        System.setProperty("spring.ai.ollama.chat.options.temperature",
                llmProperties.getChat().getTemperature().toString());
        System.setProperty("spring.ai.ollama.embedding.options.model",
                llmProperties.getEmbedding().getModel());
    }
}


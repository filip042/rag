package cz.cuni.mff.hanaf.mainapp.providers;

import org.springframework.stereotype.Component;

@Component
public class OllamaProviderStrategy implements LlmProviderStrategy {

    @Override
    public boolean supports(String provider) {
        return "ollama".equalsIgnoreCase(provider);
    }

    @Override
    public void apply(LlmProperties properties) {
        System.setProperty("spring.ai.ollama.base-url", properties.getBaseUrl());
        System.setProperty("spring.ai.ollama.chat.options.model", properties.getChat().getModel());
        System.setProperty("spring.ai.ollama.chat.options.temperature",
                properties.getChat().getTemperature().toString());
        System.setProperty("spring.ai.ollama.embedding.options.model", properties.getEmbedding().getModel());
    }
}

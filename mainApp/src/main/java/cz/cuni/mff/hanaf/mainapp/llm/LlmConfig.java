package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean
    public LlmMethods llmMethods(OllamaApi ollamaApi, @Value("${spring.ai.ollama.chat.options.model}") String modelName) {
        return switch (modelName) {
            case String s when s.startsWith("deepseek-r1:") -> new DeepseekMethods(ollamaApi, modelName);
            case String s when s.startsWith("qwen3:") -> new Qwen3Methods(ollamaApi, modelName);
            default -> throw new IllegalArgumentException("Unknown model: " + modelName);
        };
    }
}

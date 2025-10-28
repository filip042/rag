package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean
    @ConditionalOnProperty(
            name = "spring.ai.ollama.chat.options.model",
            havingValue = "deepseek-r1:1.5b"// "qwen3:latest"
//            matchIfMissing = true
    )
    public LlmMethods deepseekMethods(OllamaApi ollamaApi) {
        return new DeepseekMethods(ollamaApi);
    }

    @Bean
    @ConditionalOnProperty(
            name = "spring.ai.ollama.chat.options.model",
            havingValue = "qwen3:0.6b"
    )

    public LlmMethods qwen3Methods(OllamaApi ollamaApi) {
        return new Qwen3Methods(ollamaApi);
    }
}

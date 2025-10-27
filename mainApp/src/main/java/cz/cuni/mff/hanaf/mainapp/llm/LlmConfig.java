package cz.cuni.mff.hanaf.mainapp.llm;

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
    public LlmMethods qwen3Methods() {
        return new Qwen3Methods();
    }
}

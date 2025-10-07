package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Bean
    @ConditionalOnProperty(
            name = "spring.ai.openai.chat.options.model",
            havingValue = "qwen3:latest"
//            matchIfMissing = true
    )
    public LlmMethods qwen3Methods() {
        return new Qwen3Methods();
    }
}

package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LlmConfig {
    @Bean
    public LlmMethods llmMethods(LlmProperties llmProperties, List<LlmMethodsFactory> factories) {
        String modelName = llmProperties.getChat().getModel();
        return factories.stream()
                .filter(f -> f.supports(modelName))
                .findFirst()
                .map(f -> f.create(modelName))
                .orElseThrow(() -> new IllegalArgumentException("Unknown model: " + modelName));
    }
}

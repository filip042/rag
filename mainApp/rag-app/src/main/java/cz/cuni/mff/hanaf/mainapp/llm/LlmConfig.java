package cz.cuni.mff.hanaf.mainapp.llm;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import cz.cuni.mff.hanaf.core.config.LlmProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LlmConfig {
    @Bean
    public LlmMethods llmMethods(LlmProperties llmProperties, List<LlmMethodsFactory> factories) {
        String providerName = llmProperties.getProvider();
        String modelName = llmProperties.getChat().getModel();
        return factories.stream()
                .filter(f -> f.supports(providerName, modelName))
                .findFirst()
                .map(f -> f.create(modelName))
                .orElseThrow(() -> new IllegalArgumentException("Unknown model: " + modelName));
    }
}

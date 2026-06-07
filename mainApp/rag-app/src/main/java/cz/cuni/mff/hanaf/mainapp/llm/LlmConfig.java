package cz.cuni.mff.hanaf.mainapp.llm;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import cz.cuni.mff.hanaf.core.config.LlmProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the {@link LlmMethods} bean based on the provider and model set in the application properties.
 */
@Configuration
public class LlmConfig {

    /**
     * Creates an {@link LlmMethods} instance by selecting the first factory that supports
     * the configured provider and model.
     *
     * @param llmProperties the LLM configuration properties
     * @param factories the list of available {@link LlmMethodsFactory} implementations
     * @return an {@link LlmMethods} instance for the configured model
     * @throws IllegalArgumentException if no factory supports the configured provider and model
     */
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

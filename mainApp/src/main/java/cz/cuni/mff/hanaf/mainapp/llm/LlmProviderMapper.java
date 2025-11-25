package cz.cuni.mff.hanaf.mainapp.llm;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmProviderMapper {

    private final LlmProperties llmProperties;
    private final List<LlmProviderStrategy> strategies;

    public LlmProviderMapper(LlmProperties llmProperties, List<LlmProviderStrategy> strategies) {
        this.llmProperties = llmProperties;
        this.strategies = strategies;
    }

    @PostConstruct
    public void mapProperties() {
        String provider = llmProperties.getProvider().toLowerCase();

        for (LlmProviderStrategy strategy : strategies) {
            if (strategy.supports(provider)) {
                strategy.apply(llmProperties);
                System.out.println("Applied properties for provider: " + provider);
                return;
            }
        }

        throw new IllegalArgumentException("No LLM provider strategy found for: " + provider);
    }
}

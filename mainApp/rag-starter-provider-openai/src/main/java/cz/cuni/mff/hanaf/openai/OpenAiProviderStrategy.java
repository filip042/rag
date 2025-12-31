package cz.cuni.mff.hanaf.openai;

import cz.cuni.mff.hanaf.core.config.LlmProperties;
import cz.cuni.mff.hanaf.core.llm.LlmProviderStrategy;
import org.springframework.stereotype.Component;

@Component
public class OpenAiProviderStrategy implements LlmProviderStrategy {

    @Override
    public boolean supports(String provider) {
        return "openai".equalsIgnoreCase(provider);
    }

    @Override
    public void apply(LlmProperties properties) {
        System.setProperty("spring.ai.openai.api-key", properties.getApiKey());
        System.setProperty("spring.ai.openai.chat.options.model", properties.getChat().getModel());
        System.setProperty("spring.ai.openai.chat.options.temperature",
                properties.getChat().getTemperature().toString());
        System.setProperty("spring.ai.openai.embedding.options.model", properties.getEmbedding().getModel());
    }
}

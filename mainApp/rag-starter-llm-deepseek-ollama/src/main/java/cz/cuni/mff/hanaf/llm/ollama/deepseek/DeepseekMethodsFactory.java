package cz.cuni.mff.hanaf.llm.ollama.deepseek;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.ollama.OllamaGenericMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * {@link OllamaGenericMethodsFactory} specialization for DeepSeek R1 models
 */
@Component
public class DeepseekMethodsFactory extends OllamaGenericMethodsFactory {

    /**
     * Creates a new {@code DeepseekMethodsFactory}.
     *
     * @param ollamaChatModel the Ollama chat model to pass to created instances
     */
    public DeepseekMethodsFactory(OllamaChatModel ollamaChatModel) {
        super(ollamaChatModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String providerName, String modelName) {
        return super.supports(providerName, modelName) && modelName.toLowerCase().startsWith("deepseek-r1:");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmMethods create(String modelName) {
        return new DeepseekMethods(ollamaChatModel, modelName);
    }
}

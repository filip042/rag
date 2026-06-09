package cz.cuni.mff.hanaf.llm.ollama;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * {@link LlmMethodsFactory} implementation for Ollama-hosted models.
 * Acts as the fallback factory for any Ollama provider, regardless of model name.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class OllamaGenericMethodsFactory implements LlmMethodsFactory {

    /**
     * The Ollama chat model used to make LLM calls.
     */
    protected final OllamaChatModel ollamaChatModel;

    /**
     * Creates a new {@code OllamaGenericMethodsFactory}.
     *
     * @param ollamaChatModel the Ollama chat model to pass to created instances
     */
    public OllamaGenericMethodsFactory(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String providerName, String modelName) {
        return providerName.equalsIgnoreCase("ollama");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmMethods create(String modelName) {
        return new OllamaGenericMethods(ollamaChatModel, modelName);
    }
}

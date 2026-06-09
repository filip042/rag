package cz.cuni.mff.hanaf.llm.openai;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * {@link LlmMethodsFactory} implementation for OpenAI-hosted models.
 * Acts as the fallback factory for any OpenAI provider, regardless of model name.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class OpenAiGenericMethodsFactory implements LlmMethodsFactory {

    /**
     * The OpenAI chat model used to make LLM calls.
     */
    protected final OpenAiChatModel openAiChatModel;

    /**
     * Creates a new {@code OpenAiGenericMethodsFactory}.
     *
     * @param openAiChatModel the OpenAI chat model to pass to created instances
     */
    public OpenAiGenericMethodsFactory(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String providerName, String modelName) {
        return providerName.equalsIgnoreCase("openai");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmMethods create(String modelName) {
        return new OpenAiGenericMethods(openAiChatModel, modelName);
    }
}
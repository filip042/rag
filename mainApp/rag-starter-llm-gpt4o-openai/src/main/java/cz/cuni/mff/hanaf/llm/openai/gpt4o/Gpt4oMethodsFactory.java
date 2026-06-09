package cz.cuni.mff.hanaf.llm.openai.gpt4o;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.openai.OpenAiGenericMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

/**
 * {@link OpenAiGenericMethodsFactory} specialization for GPT-4o models
 */
@Component
public class Gpt4oMethodsFactory extends OpenAiGenericMethodsFactory {

    /**
     * Creates a new {@code Gpt4oMethodsFactory}.
     *
     * @param openAiChatModel the OpenAI chat model to pass to created instances
     */
    public Gpt4oMethodsFactory(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String providerName, String modelName) {
        return super.supports(providerName, modelName) && modelName.toLowerCase().startsWith("gpt-4o");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmMethods create(String modelName) {
        return new Gpt4oMethods(openAiChatModel, modelName);
    }
}

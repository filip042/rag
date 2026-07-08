package cz.cuni.mff.hanaf.llm.openai.gpt4o;

import cz.cuni.mff.hanaf.llm.openai.OpenAiGenericMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;

/**
 * {@link OpenAiGenericMethods} specialization for GPT-4o models.
 */
public class Gpt4oMethods extends OpenAiGenericMethods {

    private static final Logger logger = LoggerFactory.getLogger(Gpt4oMethods.class);

    /**
     * Creates a new {@code Gpt4oMethods} for the given model.
     *
     * @param openAiChatModel the OpenAi chat model to use
     * @param model the model name to pass in chat options
     */
    public Gpt4oMethods(OpenAiChatModel openAiChatModel, String model) {
        super(openAiChatModel, model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String callWithoutThinking(String prompt) {
        logger.debug("Gpt4o without thinking: {}", prompt);
        return super.callWithoutThinking(prompt);
    }
}

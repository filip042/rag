package cz.cuni.mff.hanaf.llm.ollama.qwen3;

import cz.cuni.mff.hanaf.llm.ollama.OllamaGenericMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;

/**
 * {@link OllamaGenericMethods} specialization for Qwen3 models.
 */
public class Qwen3Methods extends OllamaGenericMethods {

    private static final Logger logger = LoggerFactory.getLogger(Qwen3Methods.class);

    /**
     * Creates a new {@code Qwen3Methods} for the given model.
     *
     * @param ollamaChatModel the Ollama chat model to use
     * @param model the model name to pass in chat options
     */
    public Qwen3Methods(OllamaChatModel ollamaChatModel, String model) {
        super(ollamaChatModel, model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String callWithoutThinking(String prompt) {
        logger.debug("Qwen3 without thinking: {}", prompt);
        return super.callWithoutThinking(prompt);
    }
}

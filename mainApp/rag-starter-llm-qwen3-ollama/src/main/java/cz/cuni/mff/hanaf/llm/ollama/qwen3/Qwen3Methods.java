package cz.cuni.mff.hanaf.llm.ollama.qwen3;

import cz.cuni.mff.hanaf.llm.ollama.OllamaGenericMethods;
import org.springframework.ai.ollama.OllamaChatModel;

/**
 * {@link OllamaGenericMethods} specialization for Qwen3 models.
 */
public class Qwen3Methods extends OllamaGenericMethods {

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
        System.out.println("qwen3");
        return super.callWithoutThinking(prompt);
    }
}

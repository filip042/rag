package cz.cuni.mff.hanaf.llm.ollama.deepseek;

import cz.cuni.mff.hanaf.llm.ollama.OllamaGenericMethods;
import org.springframework.ai.ollama.OllamaChatModel;

/**
 * {@link OllamaGenericMethods} specialization for DeepSeek models.
 */
public class DeepseekMethods extends OllamaGenericMethods {

    /**
     * Creates a new {@code DeepseekMethods} for the given model.
     *
     * @param ollamaChatModel the Ollama chat model to use
     * @param model the model name to pass in chat options
     */
    public DeepseekMethods(OllamaChatModel ollamaChatModel, String model) {
        super(ollamaChatModel, model);
    }

    /**
     * Removes the thought process from the given output. Also cleans up common words used by the LLM to introduce the answer
     *
     * @param withThinking The output of the LLM with the thought process
     * @return The output of the LLM without the thought process
     */
    @Override
    public String removeThinking(String withThinking) {
        String cleaned = withThinking.replaceAll("(?s)<think>.*?</think>", "");
        cleaned = cleaned.replaceAll("(?is)^.*?\\banswer\\s*[:\\-–]\\s*", "");
        cleaned = cleaned.replaceAll("\\bdone\\b", "");
        cleaned = cleaned.replaceAll("\\*+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String callWithoutThinking(String prompt) {
        System.out.println("deepseek");
        return super.callWithoutThinking(prompt);
    }
}

package cz.cuni.mff.hanaf.llm.ollama.deepseek;

import cz.cuni.mff.hanaf.ollama.OllamaGenericMethods;
import org.springframework.ai.ollama.OllamaChatModel;

public class DeepseekMethods extends OllamaGenericMethods {

    public DeepseekMethods(OllamaChatModel ollamaChatModel, String model) {
        super(ollamaChatModel, model);
    }

    /**
     * Removes the thought process from the given output
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

    @Override
    public String callWithoutThinking(String prompt) {
        System.out.println("deepseek");
        return super.callWithoutThinking(prompt);
    }
}

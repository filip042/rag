package cz.cuni.mff.hanaf.llm.deepseek;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class DeepseekMethods implements LlmMethods {
    private final OllamaChatModel ollamaChatModel;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public DeepseekMethods(OllamaChatModel ollamaChatModel, String model) {
        this.ollamaChatModel = ollamaChatModel;
        this.model = model;
    }

    /**
     * Gets the prompt template for relevance checking
     *
     * @return The prompt template for relevance checking
     */
    @Override
    public Resource getResource() {
        return systemResource;
    }

    /**
     * Removes the thought process from the given output
     *
     * @param withThinking The output of the LLM with the thought process
     * @return The output of the LLM without the thought process
     */
    public String removeThinking(String withThinking) {
        String cleaned = withThinking.replaceAll("(?s)<think>.*?</think>", "");
        cleaned = cleaned.replaceAll("(?is)^.*?\\banswer\\s*[:\\-–]\\s*", "");
        cleaned = cleaned.replaceAll("\\bdone\\b", "");
        cleaned = cleaned.replaceAll("\\*+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    public String callWithoutThinking(String prompt) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .disableThinking()
                .build();

        Prompt chatPrompt = new Prompt(prompt, options);
        return ollamaChatModel.call(chatPrompt).getResult().getOutput().toString();
    }
}

package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Qwen3Methods implements LlmMethods {
    private final OllamaChatModel ollamaChatModel;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public Qwen3Methods(OllamaChatModel ollamaChatModel, String model) {
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
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
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

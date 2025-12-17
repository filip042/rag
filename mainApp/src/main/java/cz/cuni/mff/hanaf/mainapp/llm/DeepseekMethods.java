package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.*;

public class DeepseekMethods implements LlmMethods {
    private final OllamaApi ollamaApi;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public DeepseekMethods(OllamaApi ollamaApi, String model) {
        this.ollamaApi = ollamaApi;
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
        Map<String, Object> options = OllamaChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .disableThinking()
                .build()
                .toMap();

        OllamaApi.ChatRequest request = OllamaApi.ChatRequest.builder(model)
                .stream(false)
                .messages(List.of(
                        OllamaApi.Message.builder(OllamaApi.Message.Role.USER)
                                .content(prompt)
                                .build()))
                .options(options)
                .build();

        return ollamaApi.chat(request).message().content();
    }
}

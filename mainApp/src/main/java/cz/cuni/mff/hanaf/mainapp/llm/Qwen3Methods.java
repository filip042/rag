package cz.cuni.mff.hanaf.mainapp.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Qwen3Methods implements LlmMethods {
    private final OllamaApi ollamaApi;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public Qwen3Methods(OllamaApi ollamaApi, String model) {
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
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
    }

    public String callWithoutThinking(String prompt) {
        Map<String, Object> options = OllamaChatOptions.builder()
                .model("qwen3:0.6b")
                .temperature(0.7)
                .disableThinking()
                .build()
                .toMap();

        OllamaApi.ChatRequest request = OllamaApi.ChatRequest.builder("qwen3:0.6b")
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

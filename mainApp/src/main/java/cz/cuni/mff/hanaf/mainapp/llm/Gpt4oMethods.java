package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gpt4oMethods implements LlmMethods {
    private final OpenAiApi openAiApi;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public Gpt4oMethods(OpenAiApi openAiApi, String model) {
        this.openAiApi = openAiApi;
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
        OpenAiApi.ChatCompletionMessage chatCompletionMessage =
                new OpenAiApi.ChatCompletionMessage(prompt, OpenAiApi.ChatCompletionMessage.Role.USER);

        OpenAiApi.ChatCompletionRequest request = new OpenAiApi.ChatCompletionRequest(
                List.of(chatCompletionMessage),
                model,
                0.7,    // temperature
                false   // stream
        );

        ResponseEntity<OpenAiApi.ChatCompletion> response = this.openAiApi.chatCompletionEntity(request);

        return response.getBody()
                .choices()
                .getFirst()
                .message()
                .content();
    }
}

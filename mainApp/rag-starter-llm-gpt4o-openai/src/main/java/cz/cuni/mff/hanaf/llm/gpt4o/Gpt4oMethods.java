package cz.cuni.mff.hanaf.llm.gpt4o;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gpt4oMethods implements LlmMethods {
    private final OpenAiChatModel openAiChatModel;
    private final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    public Gpt4oMethods(OpenAiChatModel openAiChatModel, String model) {
        this.openAiChatModel = openAiChatModel;
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
    public String removeThinking(String withThinking) { // todo
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
    }

    public String callWithoutThinking(String prompt) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        Prompt chatPrompt = new Prompt(prompt, options);
        return openAiChatModel.call(chatPrompt).getResult().getOutput().getText();
    }
}

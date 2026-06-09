package cz.cuni.mff.hanaf.openai;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link LlmMethods} implementation for OpenAI-hosted models.
 */
public class OpenAiGenericMethods implements LlmMethods {

    /**
     * The OpenAI chat model used to make LLM calls.
     */
    protected final OpenAiChatModel openAiChatModel;

    /**
     * The model name passed in chat options when making LLM calls.
     */
    protected final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    /**
     * Creates a new {@code OpenAiGenericMethods} for the given model.
     *
     * @param openAiChatModel the OpenAI chat model to use
     * @param model the model name to pass in chat options
     */
    public OpenAiGenericMethods(OpenAiChatModel openAiChatModel, String model) {
        this.openAiChatModel = openAiChatModel;
        this.model = model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource() {
        return systemResource;
    }

    /**
     * {@inheritDoc}
     */
    public String removeThinking(String withThinking) { // todo
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
    }

    /**
     * {@inheritDoc}
     */
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

package cz.cuni.mff.hanaf.llm.ollama;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link LlmMethods} implementation for Ollama-hosted models.
 */
public class OllamaGenericMethods implements LlmMethods {

    /**
     * The Ollama chat model used to make LLM calls.
     */
    protected final OllamaChatModel ollamaChatModel;

    /**
     * The model name passed in chat options when making LLM calls.
     */
    protected final String model;

    @Value("classpath:/prompts/check-relevance.txt")
    private Resource systemResource;

    /**
     * Creates a new {@code OllamaGenericMethods} for the given model.
     *
     * @param ollamaChatModel the Ollama chat model to use
     * @param model the model name to pass in chat options
     */
    public OllamaGenericMethods(OllamaChatModel ollamaChatModel, String model) {
        this.ollamaChatModel = ollamaChatModel;
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
    public String removeThinking(String withThinking) {
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
    }

    /**
     * {@inheritDoc}
     */
    public String callWithoutThinking(String prompt) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .disableThinking()
                .build();

        Prompt chatPrompt = new Prompt(prompt, options);
        return ollamaChatModel.call(chatPrompt).getResult().getOutput().getText();
    }
}

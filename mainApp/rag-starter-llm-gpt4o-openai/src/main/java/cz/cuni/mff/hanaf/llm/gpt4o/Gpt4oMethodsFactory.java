package cz.cuni.mff.hanaf.llm.gpt4o;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class Gpt4oMethodsFactory implements LlmMethodsFactory {

    private final OpenAiChatModel openAiChatModel;

    public Gpt4oMethodsFactory(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("gpt-4o");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Gpt4oMethods(openAiChatModel, modelName);
    }
}
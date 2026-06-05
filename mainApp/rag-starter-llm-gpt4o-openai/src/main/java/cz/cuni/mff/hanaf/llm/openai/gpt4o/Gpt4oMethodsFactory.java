package cz.cuni.mff.hanaf.llm.openai.gpt4o;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.openai.OpenAiGenericMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class Gpt4oMethodsFactory extends OpenAiGenericMethodsFactory {

    public Gpt4oMethodsFactory(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @Override
    public boolean supports(String providerName, String modelName) {
        return super.supports(providerName, modelName) && modelName.toLowerCase().startsWith("gpt-4o");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Gpt4oMethods(openAiChatModel, modelName);
    }
}
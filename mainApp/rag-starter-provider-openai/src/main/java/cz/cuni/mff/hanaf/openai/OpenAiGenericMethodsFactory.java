package cz.cuni.mff.hanaf.openai;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.LOWEST_PRECEDENCE)
public class OpenAiGenericMethodsFactory implements LlmMethodsFactory {

    private final OpenAiChatModel openAiChatModel;

    public OpenAiGenericMethodsFactory(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Override
    public boolean supports(String providerName, String modelName) {
        return providerName.equalsIgnoreCase("openai");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new OpenAiGenericMethods(openAiChatModel, modelName);
    }
}
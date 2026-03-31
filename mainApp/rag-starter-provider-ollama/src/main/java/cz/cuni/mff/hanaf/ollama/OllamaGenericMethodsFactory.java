package cz.cuni.mff.hanaf.ollama;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.LOWEST_PRECEDENCE)
public class OllamaGenericMethodsFactory implements LlmMethodsFactory {

    private final OllamaChatModel ollamaChatModel;

    public OllamaGenericMethodsFactory(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public boolean supports(String providerName, String modelName) {
        return providerName.equalsIgnoreCase("ollama");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new OllamaGenericMethods(ollamaChatModel, modelName);
    }

}

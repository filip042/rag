package cz.cuni.mff.hanaf.llm.ollama.deepseek;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.ollama.OllamaGenericMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

@Component
public class DeepseekMethodsFactory extends OllamaGenericMethodsFactory {

    public DeepseekMethodsFactory(OllamaChatModel ollamaChatModel) {
        super(ollamaChatModel);
    }

    @Override
    public boolean supports(String providerName, String modelName) {
        return super.supports(providerName, modelName) && modelName.toLowerCase().startsWith("deepseek-r1:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new DeepseekMethods(ollamaChatModel, modelName);
    }
}
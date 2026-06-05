package cz.cuni.mff.hanaf.llm.ollama.qwen3;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.ollama.OllamaGenericMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

@Component
public class Qwen3MethodsFactory extends OllamaGenericMethodsFactory {

    public Qwen3MethodsFactory(OllamaChatModel ollamaChatModel) {
        super(ollamaChatModel);
    }

    @Override
    public boolean supports(String providerName, String modelName) {
        return super.supports(providerName, modelName) && modelName.toLowerCase().startsWith("qwen3:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Qwen3Methods(ollamaChatModel, modelName);
    }
}
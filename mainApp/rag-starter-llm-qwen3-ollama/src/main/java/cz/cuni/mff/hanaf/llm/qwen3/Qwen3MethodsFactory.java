package cz.cuni.mff.hanaf.llm.qwen3;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

@Component
public class Qwen3MethodsFactory implements LlmMethodsFactory {

    private final OllamaChatModel ollamaChatModel;

    public Qwen3MethodsFactory(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("qwen3:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Qwen3Methods(ollamaChatModel, modelName);
    }
}
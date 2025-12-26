package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

@Component
public class DeepseekMethodsFactory implements LlmMethodsFactory {

    private final OllamaChatModel ollamaChatModel;

    public DeepseekMethodsFactory(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("deepseek-r1:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new DeepseekMethods(ollamaChatModel, modelName);
    }
}
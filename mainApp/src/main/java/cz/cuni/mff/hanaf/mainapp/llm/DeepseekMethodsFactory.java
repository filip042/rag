package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.stereotype.Component;

@Component
public class DeepseekMethodsFactory implements LlmMethodsFactory {

    private final OllamaApi ollamaApi;

    public DeepseekMethodsFactory(OllamaApi ollamaApi) {
        this.ollamaApi = ollamaApi;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("deepseek-r1:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new DeepseekMethods(ollamaApi, modelName);
    }
}
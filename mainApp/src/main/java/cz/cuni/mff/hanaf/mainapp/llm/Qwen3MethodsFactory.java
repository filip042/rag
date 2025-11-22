package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.stereotype.Component;

@Component
public class Qwen3MethodsFactory implements LlmMethodsFactory {

    private final OllamaApi ollamaApi;

    public Qwen3MethodsFactory(OllamaApi ollamaApi) {
        this.ollamaApi = ollamaApi;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("qwen3:");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Qwen3Methods(ollamaApi, modelName);
    }
}
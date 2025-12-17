package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class Gpt4oMethodsFactory implements LlmMethodsFactory {

    private final OpenAiApi openAiApi;

    public Gpt4oMethodsFactory(OpenAiApi openAiApi) {
        this.openAiApi = openAiApi;
    }

    @Override
    public boolean supports(String modelName) {
        return modelName.startsWith("gpt-4o");
    }

    @Override
    public LlmMethods create(String modelName) {
        return new Gpt4oMethods(openAiApi, modelName);
    }
}
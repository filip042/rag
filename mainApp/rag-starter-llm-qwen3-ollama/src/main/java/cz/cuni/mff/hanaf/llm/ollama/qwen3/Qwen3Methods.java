package cz.cuni.mff.hanaf.llm.ollama.qwen3;

import cz.cuni.mff.hanaf.ollama.OllamaGenericMethods;
import org.springframework.ai.ollama.OllamaChatModel;

public class Qwen3Methods extends OllamaGenericMethods {

    public Qwen3Methods(OllamaChatModel ollamaChatModel, String model) {
        super(ollamaChatModel, model);
    }

    @Override
    public String callWithoutThinking(String prompt) {
        System.out.println("qwen3");
        return super.callWithoutThinking(prompt);
    }
}

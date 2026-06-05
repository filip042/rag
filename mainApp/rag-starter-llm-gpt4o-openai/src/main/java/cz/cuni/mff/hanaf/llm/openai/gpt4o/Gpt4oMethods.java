package cz.cuni.mff.hanaf.llm.openai.gpt4o;

import cz.cuni.mff.hanaf.openai.OpenAiGenericMethods;
import org.springframework.ai.openai.OpenAiChatModel;

public class Gpt4oMethods extends OpenAiGenericMethods {

    public Gpt4oMethods(OpenAiChatModel openAiChatModel, String model) {
        super(openAiChatModel, model);
    }

    @Override
    public String callWithoutThinking(String prompt) {
        System.out.println("gpt4o");
        return super.callWithoutThinking(prompt);
    }
}

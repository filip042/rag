package cz.cuni.mff.hanaf.llm.gpt4o.config;

import cz.cuni.mff.hanaf.llm.gpt4o.Gpt4oMethodsFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class Gpt4oAutoConfiguration {

    @Bean
    public Gpt4oMethodsFactory gpt4oMethodsFactory(OpenAiChatModel openAiChatModel) {
        return new Gpt4oMethodsFactory(openAiChatModel);
    }
}
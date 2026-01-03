package cz.cuni.mff.hanaf.llm.deepseek.config;

import cz.cuni.mff.hanaf.llm.deepseek.DeepseekMethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DeepseekAutoConfiguration {

    @Bean
    public DeepseekMethodsFactory deepseekMethodsFactory(OllamaChatModel ollamaChatModel) {
        return new DeepseekMethodsFactory(ollamaChatModel);
    }
}
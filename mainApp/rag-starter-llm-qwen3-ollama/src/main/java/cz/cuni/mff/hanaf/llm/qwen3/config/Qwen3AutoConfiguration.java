package cz.cuni.mff.hanaf.llm.qwen3.config;

import cz.cuni.mff.hanaf.llm.qwen3.Qwen3MethodsFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class Qwen3AutoConfiguration {

    @Bean
    public Qwen3MethodsFactory deepseekMethodsFactory(OllamaChatModel ollamaChatModel) {
        return new Qwen3MethodsFactory(ollamaChatModel);
    }
}
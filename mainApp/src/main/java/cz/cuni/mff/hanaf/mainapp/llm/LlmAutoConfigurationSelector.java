package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration;

@Configuration
public class LlmAutoConfigurationSelector {

    @Configuration
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
    @Import({
            OpenAiChatAutoConfiguration.class,
            OpenAiEmbeddingAutoConfiguration.class
    })
    static class OpenAiAutoConfig {}

    @Configuration
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
    @Import({
            OllamaChatAutoConfiguration.class,
            OllamaEmbeddingAutoConfiguration.class
    })
    static class OllamaAutoConfig {}
}

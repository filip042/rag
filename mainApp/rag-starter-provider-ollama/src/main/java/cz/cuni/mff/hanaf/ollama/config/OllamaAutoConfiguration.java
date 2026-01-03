package cz.cuni.mff.hanaf.ollama.config;

import cz.cuni.mff.hanaf.ollama.OllamaGenericMethodsFactory;
import cz.cuni.mff.hanaf.ollama.OllamaProviderStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
public class OllamaAutoConfiguration {

    @Bean
    public OllamaProviderStrategy ollamaProviderStrategy() {
        return new OllamaProviderStrategy();
    }

    @Bean
    public OllamaGenericMethodsFactory ollamaGenericMethodsFactory(OllamaChatModel ollamaChatModel) {
        return new OllamaGenericMethodsFactory(ollamaChatModel);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
    public ChatModel ollamaChatModelBean(OllamaChatModel ollamaChatModel) {
        return ollamaChatModel;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
    public EmbeddingModel ollamaEmbeddingModelBean(OllamaEmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }
}
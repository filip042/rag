package cz.cuni.mff.hanaf.mainapp.llm;

import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.core.llm.LlmMethodsFactory;
import cz.cuni.mff.hanaf.core.config.LlmProperties;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.ollama.OllamaEmbeddingModel;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.openai.OpenAiEmbeddingModel;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class LlmConfig {
    @Bean
    public LlmMethods llmMethods(LlmProperties llmProperties, List<LlmMethodsFactory> factories) {
        String modelName = llmProperties.getChat().getModel();
        System.out.println("modelName: " + modelName);
        System.out.println("factories: " + factories);
        return factories.stream()
                .filter(f -> f.supports(modelName))
                .findFirst()
                .map(f -> f.create(modelName))
                .orElseThrow(() -> new IllegalArgumentException("Unknown model: " + modelName));
    }

//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
//    public ChatModel openAiChatModelBean(OpenAiChatModel openAiChatModel) {
//        return openAiChatModel;
//    }
//
//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
//    public ChatModel ollamaChatModelBean(OllamaChatModel ollamaChatModel) {
//        return ollamaChatModel;
//    }
//
//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
//    public EmbeddingModel openAiEmbeddingModelBean(OpenAiEmbeddingModel openAiEmbeddingModel) {
//        return openAiEmbeddingModel;
//    }
//
//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
//    public EmbeddingModel ollamaEmbeddingModelBean(OllamaEmbeddingModel ollamaEmbeddingModel) {
//        return ollamaEmbeddingModel;
//    }
}

package cz.cuni.mff.hanaf.mainapp.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluatorConfig {

    private final ChatModel evaluatorChatModel;

    public EvaluatorConfig(@Value("${spring.ai.openai.api-key}") String apiKey) {
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
        this.evaluatorChatModel =
                OpenAiChatModel.builder()
                        .openAiApi(api)
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .build())
                        .build();
    }

    @Bean
    public RelevancyEvaluator relevancyEvaluator() {
        return new RelevancyEvaluator(ChatClient.builder(evaluatorChatModel));
    }

    @Bean
    public FactCheckingEvaluator factCheckingEvaluator() {
        return FactCheckingEvaluator
                .builder(ChatClient.builder(evaluatorChatModel))
                .build();
    }
}
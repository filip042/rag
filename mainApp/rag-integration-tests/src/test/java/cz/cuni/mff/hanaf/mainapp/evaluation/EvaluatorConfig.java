package cz.cuni.mff.hanaf.mainapp.evaluation;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluatorConfig {

    private final ChatModel evaluatorChatModel;
    private final ChatClient.Builder evaluatorChatClientBuilder;

    public EvaluatorConfig(@Value("${spring.ai.anthropic.api-key}") String apiKey) {
        AnthropicApi api = AnthropicApi.builder()
                .apiKey(apiKey)
                .build();
        this.evaluatorChatModel =
                AnthropicChatModel.builder()
                        .anthropicApi(api)
                        .defaultOptions(AnthropicChatOptions.builder()
                                .model("claude-haiku-4-5")
                                .maxTokens(1024)
                                .temperature(0.0)
                                .build())
                        .build();

        this.evaluatorChatClientBuilder = ChatClient.builder(evaluatorChatModel)
                .defaultSystem("""
                    You are a strict binary classifier.
                    Respond with exactly one word: yes or no.
                    Do not include punctuation, explanation, or any other text.
                    Your entire response must be only the single word "yes" or "no".
                    """);
    }

    @Bean
    public RelevancyEvaluator relevancyEvaluator() {
        return new RelevancyEvaluator(evaluatorChatClientBuilder);
    }

    @Bean
    public FactCheckingEvaluator factCheckingEvaluator() {
        return FactCheckingEvaluator
                .builder(evaluatorChatClientBuilder)
                .evaluationPrompt("""
                        Evaluate whether the following answer contains any claims that are contradicted by or absent from the provided context documents.\s
                        Respond with "yes" if the answer is fully consistent with the context, or "no" if it contains unsupported or contradicted claims.
                        
                        Context:
                        {document}
                        
                        Answer:
                        {claim}
                        """)
                .build();
    }
}
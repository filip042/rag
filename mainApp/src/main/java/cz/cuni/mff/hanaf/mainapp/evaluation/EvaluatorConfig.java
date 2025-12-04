package cz.cuni.mff.hanaf.mainapp.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluatorConfig {

    @Bean
    public RelevancyEvaluator relevancyEvaluator(ChatClient.Builder builder) {
        return new RelevancyEvaluator(builder);
    }

    @Bean
    public FactCheckingEvaluator factCheckingEvaluator(ChatClient.Builder builder) {
        return FactCheckingEvaluator.builder(builder).build();
    }
}
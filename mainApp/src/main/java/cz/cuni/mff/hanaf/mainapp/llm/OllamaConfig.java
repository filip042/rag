package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OllamaChatProperties.class)
public class OllamaConfig {

    @Bean
    public OllamaApi ollamaApi(OllamaChatProperties props) {
        return OllamaApi.builder()
                .webClientBuilder(WebClient.builder().baseUrl("http://localhost:8081") // todo from yaml
                        .filter(ExchangeFilterFunctions.basicAuthentication("username", "password"))) // todo ditto
                .build();
    }
}

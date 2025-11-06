package cz.cuni.mff.hanaf.mainapp.llm;

import cz.cuni.mff.hanaf.mainapp.AppConfig;
import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties({OllamaConnectionProperties.class, AppConfig.class})
public class OllamaConfig {
    @Primary
    @Bean
    public OllamaApi ollamaApi(OllamaConnectionProperties ollamaProps, AppConfig appConfig) {
        ExchangeFilterFunction basicAuthFilter = ExchangeFilterFunction.ofRequestProcessor(request -> {
            ClientRequest filtered = ClientRequest.from(request)
                    .headers(h -> h.setBasicAuth(appConfig.getOllama().getUsername(), appConfig.getOllama().getPassword()))
                    .build();
            return Mono.just(filtered);
        });

        WebClient.Builder webClientBuilder = WebClient.builder()
                .filter(basicAuthFilter);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBasicAuth(appConfig.getOllama().getUsername(), appConfig.getOllama().getPassword());
                    return execution.execute(request, body);
                });

        return OllamaApi.builder()
                .baseUrl(ollamaProps.getBaseUrl())
                .webClientBuilder(webClientBuilder)
                .restClientBuilder(restClientBuilder)
                .build();
    }
}

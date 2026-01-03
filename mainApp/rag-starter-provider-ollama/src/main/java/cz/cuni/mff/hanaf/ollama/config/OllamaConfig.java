package cz.cuni.mff.hanaf.ollama.config;

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
@EnableConfigurationProperties({OllamaConnectionProperties.class, OllamaAuthProperties.class})
public class OllamaConfig {
    @Primary
    @Bean
    public OllamaApi ollamaApi(OllamaConnectionProperties ollamaProps, OllamaAuthProperties authProps) {
        ExchangeFilterFunction basicAuthFilter = ExchangeFilterFunction.ofRequestProcessor(request -> {
            ClientRequest filtered = ClientRequest.from(request)
                    .headers(h -> h.setBasicAuth(authProps.getUsername(), authProps.getPassword()))
                    .build();
            return Mono.just(filtered);
        });

        WebClient.Builder webClientBuilder = WebClient.builder()
                .filter(basicAuthFilter);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBasicAuth(authProps.getUsername(), authProps.getPassword());
                    return execution.execute(request, body);
                });

        return OllamaApi.builder()
                .baseUrl(ollamaProps.getBaseUrl())
                .webClientBuilder(webClientBuilder)
                .restClientBuilder(restClientBuilder)
                .build();
    }
}

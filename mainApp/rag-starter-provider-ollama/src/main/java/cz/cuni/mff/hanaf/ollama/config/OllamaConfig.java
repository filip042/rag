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

/**
 * Configures the {@link OllamaApi} bean with basic authentication headers.
 * Necessary because Spring AI's Ollama integration does not natively support
 * authentication, so credentials are injected manually into both the synchronous
 * and reactive HTTP clients.
 */
@Configuration
@EnableConfigurationProperties({OllamaConnectionProperties.class, OllamaAuthProperties.class})
public class OllamaConfig {

    /**
     * Creates a primary {@link OllamaApi} bean configured with the Ollama base URL
     * and authentication credentials from {@link OllamaAuthProperties}.
     *
     * @param ollamaProps the Ollama connection properties providing the base URL
     * @param authProps the authentication properties providing the username and password
     * @return a configured {@link OllamaApi} instance
     */
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

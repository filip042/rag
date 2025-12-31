package cz.cuni.mff.hanaf.mainapp.llm;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class AiHttpConfig {

    /**
     * Ensures HTTP response bodies can be read multiple times.
     * Required for Spring AI clients when observation, retry,
     * or logging interceptors are enabled.
     */
    @Bean
    RestClientCustomizer bufferingRestClientCustomizer() {
        return builder -> builder
                .requestFactory(
                        new BufferingClientHttpRequestFactory(
                                new SimpleClientHttpRequestFactory()
                        )
                );
    }
}

package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.core.llm.LlmProperties;
import cz.cuni.mff.hanaf.core.vectorstore.VectorstoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main entry point for the application.
 */
@SpringBootApplication
@EnableConfigurationProperties({LlmProperties.class, VectorstoreProperties.class})
public class MainAppApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(MainAppApplication.class, args);
	}

	/**
	 * Creates a {@link RestTemplate} bean for making HTTP requests.
	 *
	 * @param builder the builder provided by Spring Boot
	 * @return the configured REST template
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}

package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.core.llm.LlmProperties;
import cz.cuni.mff.hanaf.core.vectorstore.VectorstoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

/**
 * Main entry point for the application.
 */
@SpringBootApplication
@EnableConfigurationProperties({LlmProperties.class, VectorstoreProperties.class})
public class MainAppApplication {

	private static final Logger logger = LoggerFactory.getLogger(MainAppApplication.class);

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(MainAppApplication.class, args);
	}

	/**
	 * Logs a clearly visible banner once the application is fully started
	 * and ready to serve requests.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void logReadyBanner() {
		logger.info("==========================================================");
		logger.info("  Application is ready -- open {} in a browser", baseUrl);
		logger.info("  Stop the stack with: docker compose down");
		logger.info("==========================================================");
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

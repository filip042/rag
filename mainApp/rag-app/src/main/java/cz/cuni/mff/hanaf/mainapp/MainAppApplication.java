package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.core.config.LlmProperties;
import cz.cuni.mff.hanaf.core.vectorstore.VectorstoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties({LlmProperties.class, VectorstoreProperties.class})
public class MainAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainAppApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}

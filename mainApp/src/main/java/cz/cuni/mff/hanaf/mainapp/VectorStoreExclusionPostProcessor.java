package cz.cuni.mff.hanaf.mainapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class VectorStoreExclusionPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String provider = environment.getProperty("app.vectorstore.provider", "elasticsearch");

        Map<String, Object> props = new HashMap<>();

        if ("elasticsearch".equalsIgnoreCase(provider)) {
            props.put("spring.autoconfigure.exclude",
                    "org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchVectorStoreAutoConfiguration");
        } else if ("opensearch".equalsIgnoreCase(provider)) {
            props.put("spring.autoconfigure.exclude",
                    "org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration");
        }

        environment.getPropertySources().addFirst(new MapPropertySource("vectorStoreExclusion", props));
    }
}
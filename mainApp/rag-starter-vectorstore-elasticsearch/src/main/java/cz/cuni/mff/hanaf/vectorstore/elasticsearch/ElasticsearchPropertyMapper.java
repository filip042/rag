package cz.cuni.mff.hanaf.vectorstore.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link EnvironmentPostProcessor} that maps {@code app.vectorstore.*} properties to their
 * corresponding Spring AI Elasticsearch properties when the configured provider is Elasticsearch.
 */
public class ElasticsearchPropertyMapper implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchPropertyMapper.class);

    /**
     * Maps {@code app.vectorstore.*} properties to Spring AI Elasticsearch properties if the provider is Elasticsearch.
     *
     * @param environment the environment to post-process
     * @param application the application instance
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String provider = environment.getProperty("app.vectorstore.provider", "").toLowerCase();

        if (!"elasticsearch".equals(provider)) {
            return;
        }

        Map<String, Object> mappedProps = new HashMap<>();

        mapProperty(environment, mappedProps, "app.vectorstore.uris", "spring.elasticsearch.uris");
        mapProperty(environment, mappedProps, "app.vectorstore.username", "spring.elasticsearch.username");
        mapProperty(environment, mappedProps, "app.vectorstore.password", "spring.elasticsearch.password");
        mapProperty(environment, mappedProps, "app.vectorstore.initialize-schema", "spring.ai.vectorstore.elasticsearch.initialize-schema");
        mapProperty(environment, mappedProps, "app.vectorstore.index-name", "spring.ai.vectorstore.elasticsearch.index-name");
        mapProperty(environment, mappedProps, "app.vectorstore.dimensions", "spring.ai.vectorstore.elasticsearch.dimensions");
        mapProperty(environment, mappedProps, "app.vectorstore.similarity", "spring.ai.vectorstore.elasticsearch.similarity");

        logger.debug("Mapped properties = {}", mappedProps);

        if (!mappedProps.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("elasticsearchVectorstoreMapping", mappedProps)
            );
        }
    }

    private void mapProperty(ConfigurableEnvironment environment, Map<String, Object> target,
                             String sourceKey, String targetKey) {
        String value = environment.getProperty(sourceKey);
        if (value != null) {
            target.put(targetKey, value);
        }
    }
}

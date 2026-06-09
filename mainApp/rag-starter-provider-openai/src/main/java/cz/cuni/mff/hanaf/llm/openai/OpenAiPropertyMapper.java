package cz.cuni.mff.hanaf.llm.openai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link EnvironmentPostProcessor} that maps {@code app.llm.*} properties to their
 * corresponding Spring AI OpenAI properties when the configured provider is OpenAI.
 */
public class OpenAiPropertyMapper implements EnvironmentPostProcessor {

    /**
     * Maps {@code app.llm.*} properties to Spring AI OpenAI properties if the provider is OpenAI.
     *
     * @param environment the environment to post-process
     * @param application the application instance
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String provider = environment.getProperty("app.llm.provider", "").toLowerCase();

        if (!"openai".equals(provider)) {
            return;
        }

        Map<String, Object> mappedProps = new HashMap<>();
        mapProperty(environment, mappedProps, "app.llm.api-key", "spring.ai.openai.api-key");
        mapProperty(environment, mappedProps, "app.llm.chat.model", "spring.ai.openai.chat.options.model");
        mapProperty(environment, mappedProps, "app.llm.chat.temperature", "spring.ai.openai.chat.options.temperature");
        mapProperty(environment, mappedProps, "app.llm.embedding.model", "spring.ai.openai.embedding.options.model");

        if (!mappedProps.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("openaiLlmMapping", mappedProps));
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

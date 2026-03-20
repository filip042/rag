package cz.cuni.mff.hanaf.ollama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class OllamaPropertyMapper implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String provider = environment.getProperty("app.llm.provider", "").toLowerCase();

        if (!"ollama".equals(provider)) {
            return;
        }

        Map<String, Object> mappedProps = new HashMap<>();
        mapProperty(environment, mappedProps, "app.llm.base-url", "spring.ai.ollama.base-url");
        mapProperty(environment, mappedProps, "app.llm.chat.model", "spring.ai.ollama.chat.options.model");
        mapProperty(environment, mappedProps, "app.llm.chat.temperature", "spring.ai.ollama.chat.options.temperature");
        mapProperty(environment, mappedProps, "app.llm.embedding.model", "spring.ai.ollama.embedding.options.model");

        if (!mappedProps.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("ollamaLlmMapping", mappedProps));
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

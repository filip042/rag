package cz.cuni.mff.hanaf.mainapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicLlmConfig {

    @Bean
    public static PropertySource<?> remapProperties(ConfigurableEnvironment env) {
        Map<String, Object> mapped = new HashMap<>();

        String provider = env.getProperty("app.llm.provider", "ollama").toLowerCase();

        if ("ollama".equals(provider)) {
            mapped.put("spring.ai.ollama.chat.options.model", env.getProperty("app.llm.chat.model"));
            mapped.put("spring.ai.ollama.chat.options.temperature", env.getProperty("app.llm.chat.temperature"));
            mapped.put("spring.ai.ollama.embedding.options.model", env.getProperty("app.llm.embedding.model"));
            mapped.put("spring.ai.ollama.base-url", env.getProperty("app.llm.base-url"));
        }


        return new MapPropertySource("dynamic-llm-mapping", mapped);
    }
}

package cz.cuni.mff.hanaf.llm.ollama.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Authentication properties for the Ollama API, bound from the {@code app.ollama} prefix.
 */
@ConfigurationProperties(prefix = "app.ollama")
public class OllamaAuthProperties {
    private String username;
    private String password;

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }
}
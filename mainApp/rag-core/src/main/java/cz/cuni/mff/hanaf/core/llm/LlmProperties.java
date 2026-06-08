package cz.cuni.mff.hanaf.core.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the Large Language Model, bound from the {@code app.llm} prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private String provider;
    private String baseUrl;
    private String apiKey;
    private Chat chat = new Chat();
    private Embedding embedding = new Embedding();

    /**
     * Configuration properties specific to chat generation.
     */
    public static class Chat {
        private String model;
        private Double temperature;

        /**
         * Returns the identifier of the chat model to use.
         *
         * @return the chat model name
         */
        public String getModel() {
            return model;
        }

        /**
         * Sets the name of the chat model to use.
         *
         * @param model the chat model name to set
         */
        public void setModel(String model) {
            this.model = model;
        }

        /**
         * Returns the temperature parameter for chat generation.
         *
         * @return the temperature value
         */
        public Double getTemperature() {
            return temperature;
        }

        /**
         * Sets the temperature parameter for chat generation.
         *
         * @param temperature the temperature value to set
         */
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }

    /**
     * Configuration properties specific to document embedding.
     */
    public static class Embedding {
        private String model;

        /**
         * Returns the name of the embedding model to use.
         *
         * @return the embedding model name
         */
        public String getModel() {
            return model;
        }

        /**
         * Sets the name of the embedding model to use.
         *
         * @param model the embedding model name to set
         */
        public void setModel(String model) {
            this.model = model;
        }
    }

    /**
     * Returns the used LLM provider.
     *
     * @return the LLM provider name
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the LLM provider to use.
     *
     * @param provider the LLM provider name to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the base URL for the LLM API.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL for the LLM API.
     *
     * @param baseUrl the base URL to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the API key used for authenticating with the LLM provider.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key used for authenticating with the LLM provider.
     *
     * @param apiKey the API key to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the chat-specific configuration properties.
     *
     * @return the chat configuration
     */
    public Chat getChat() {
        return chat;
    }

    /**
     * Sets the chat-specific configuration properties.
     *
     * @param chat the chat configuration to set
     */
    public void setChat(Chat chat) {
        this.chat = chat;
    }

    /**
     * Returns the embedding-specific configuration properties.
     *
     * @return the embedding configuration
     */
    public Embedding getEmbedding() {
        return embedding;
    }

    /**
     * Sets the embedding-specific configuration properties.
     *
     * @param embedding the embedding configuration to set
     */
    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }
}

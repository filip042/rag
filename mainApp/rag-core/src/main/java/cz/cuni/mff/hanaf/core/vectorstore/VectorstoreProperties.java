package cz.cuni.mff.hanaf.core.vectorstore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the vector database, bound from the {@code app.vectorstore} prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app.vectorstore")
public class VectorstoreProperties {

    private String provider;
    private String uris;
    private String username;
    private String password;
    private Boolean initializeSchema;
    private String indexName;
    private Integer dimensions;
    private String similarity;

    /**
     * Returns the name of the vector store provider.
     *
     * @return the vector store provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the name of the vector store provider.
     *
     * @param provider the vector store provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the connection URI(s) for the vector store database.
     *
     * @return the connection URI(s)
     */
    public String getUris() {
        return uris;
    }

    /**
     * Sets the connection URI(s) for the vector store database.
     *
     * @param uris the connection URI(s) to set
     */
    public void setUris(String uris) {
        this.uris = uris;
    }

    /**
     * Returns the username used for authenticating with the vector store.
     *
     * @return the authentication username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username used for authenticating with the vector store.
     *
     * @param username the authentication username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password used for authenticating with the vector store.
     *
     * @return the authentication password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password used for authenticating with the vector store.
     *
     * @param password the authentication password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns whether the application should automatically initialize the vector store schema/index on startup.
     *
     * @return {@code true} if the schema should be initialized, {@code false} otherwise
     */
    public Boolean isInitializeSchema() {
        return initializeSchema;
    }

    /**
     * Sets whether the application should automatically initialize the vector store schema on startup.
     *
     * @param initializeSchema {@code true} to enable initialization, {@code false} to disable it
     */
    public void setInitializeSchema(Boolean initializeSchema) {
        this.initializeSchema = initializeSchema;
    }

    /**
     * Returns the name of the index where vectors are stored.
     *
     * @return the index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Sets the name of the index where vectors are stored.
     *
     * @param indexName the index name to set
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * Returns the number of dimensions for the vectors (must match the output size of the embedding model).
     *
     * @return the vector dimensions
     */
    public Integer getDimensions() {
        return dimensions;
    }

    /**
     * Sets the number of dimensions for the vectors.
     *
     * @param dimensions the vector dimensions to set
     */
    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Returns the distance or similarity metric used for vector searches.
     *
     * @return the similarity metric
     */
    public String getSimilarity() {
        return similarity;
    }

    /**
     * Sets the distance or similarity metric used for vector searches.
     *
     * @param similarity the similarity metric to set
     */
    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }
}
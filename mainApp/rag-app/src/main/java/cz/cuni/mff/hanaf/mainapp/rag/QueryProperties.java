package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for RAG query handling, bound from the {@code app.rag.query} prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rag.query")
public class QueryProperties {
    private int maxQueryLength;

    /**
     * Returns the maximum allowed length for a query.
     *
     * @return the maximum query length
     */
    public int getMaxQueryLength() {
        return maxQueryLength;
    }

    /**
     * Sets the maximum allowed length for a query.
     *
     * @param maxQueryLength the maximum length to be set for a query
     */
    public void setMaxQueryLength(int maxQueryLength) {
        this.maxQueryLength = maxQueryLength;
    }
}

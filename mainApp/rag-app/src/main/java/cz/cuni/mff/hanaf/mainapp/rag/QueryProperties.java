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
    private String searchPrefix;
    private String documentPrefix;

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

    /**
     * Returns the prefix prepended to queries before embedding.
     *
     * @return the search prefix
     */
    public String getSearchPrefix() {
        return searchPrefix;
    }

    /**
     * Sets the prefix prepended to queries before embedding.
     *
     * @param searchPrefix the search prefix to set
     */
    public void setSearchPrefix(String searchPrefix) {
        this.searchPrefix = searchPrefix;
    }

    /**
     * Returns the prefix prepended to documents before embedding.
     *
     * @return the document prefix
     */
    public String getDocumentPrefix() {
        return documentPrefix;
    }

    /**
     * Sets the prefix prepended to documents before embedding.
     *
     * @param documentPrefix the document prefix to set
     */
    public void setDocumentPrefix(String documentPrefix) {
        this.documentPrefix = documentPrefix;
    }
}

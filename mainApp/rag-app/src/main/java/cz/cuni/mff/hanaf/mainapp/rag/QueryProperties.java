package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rag.query")
public class QueryProperties {
    private int maxQueryLength;

    public int getMaxQueryLength() {
        return maxQueryLength;
    }

    public void setMaxQueryLength(int maxQueryLength) {
        this.maxQueryLength = maxQueryLength;
    }
}
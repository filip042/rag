package cz.cuni.mff.hanaf.elasticsearch;

import cz.cuni.mff.hanaf.core.vectorstore.VectorStoreExclusionStrategy;

public class ElasticsearchExclusionStrategy implements VectorStoreExclusionStrategy {
    @Override
    public boolean supports(String provider) {
        return "elasticsearch".equalsIgnoreCase(provider);
    }

    @Override
    public String getAutoConfigurationClass() {
        return "org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration";
    }
}
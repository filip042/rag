package cz.cuni.mff.hanaf.mainapp.providers;

import cz.cuni.mff.hanaf.core.vectorstore.VectorStoreExclusionStrategy;

public class OpenSearchExclusionStrategy implements VectorStoreExclusionStrategy {
    @Override
    public boolean supports(String provider) {
        return "opensearch".equalsIgnoreCase(provider);
    }

    @Override
    public String getAutoConfigurationClass() {
        return "org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchVectorStoreAutoConfiguration";
    }
}
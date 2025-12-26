package cz.cuni.mff.hanaf.mainapp.providers;

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
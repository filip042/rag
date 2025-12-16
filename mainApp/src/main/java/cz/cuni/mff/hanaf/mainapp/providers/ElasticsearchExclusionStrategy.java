package cz.cuni.mff.hanaf.mainapp.providers;

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
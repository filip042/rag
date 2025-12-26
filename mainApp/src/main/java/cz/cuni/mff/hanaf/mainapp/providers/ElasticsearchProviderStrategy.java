package cz.cuni.mff.hanaf.mainapp.providers;

import org.springframework.stereotype.Component;

@Component
public class ElasticsearchProviderStrategy implements VectorstoreProviderStrategy {
    @Override
    public boolean supports(String provider) {
        return "elasticsearch".equalsIgnoreCase(provider);
    }

    @Override
    public void apply(VectorstoreProperties properties) {
        System.setProperty("spring.elasticsearch.uris", properties.getUris());
        System.setProperty("spring.elasticsearch.username", properties.getUsername());
        System.setProperty("spring.elasticsearch.password", properties.getPassword());
        System.setProperty("spring.ai.vectorstore.elasticsearch.initialize-schema", properties.isInitializeSchema().toString());
        System.setProperty("spring.ai.vectorstore.elasticsearch.index-name", properties.getIndexName());
        System.setProperty("spring.ai.vectorstore.elasticsearch.dimensions", properties.getDimensions().toString());
        System.out.println("boom " + properties.getDimensions());
        System.setProperty("spring.ai.vectorstore.elasticsearch.similarity", properties.getSimilarity());
    }
}

package cz.cuni.mff.hanaf.mainapp.providers;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VectorstoreProviderMapper {

    private final VectorstoreProperties vectorstoreProperties;
    private final List<VectorstoreProviderStrategy> strategies;

    public VectorstoreProviderMapper(VectorstoreProperties vectorstoreProperties, List<VectorstoreProviderStrategy> strategies) {
        this.vectorstoreProperties = vectorstoreProperties;
        this.strategies = strategies;
    }

    @PostConstruct
    public void mapProperties() {
        String provider = vectorstoreProperties.getProvider().toLowerCase();

        for (VectorstoreProviderStrategy strategy : strategies) {
            if (strategy.supports(provider)) {
                strategy.apply(vectorstoreProperties);
                System.out.println("Applied properties for provider: " + provider);
                return;
            }
        }

        throw new IllegalArgumentException("No vector store provider strategy found for: " + provider);
    }

}

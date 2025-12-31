package cz.cuni.mff.hanaf.core.vectorstore;

public interface VectorstoreProviderStrategy {
    boolean supports(String provider);
    void apply(VectorstoreProperties properties);
}

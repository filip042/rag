package cz.cuni.mff.hanaf.mainapp.providers;

public interface VectorstoreProviderStrategy {
    boolean supports(String provider);
    void apply(VectorstoreProperties properties);
}

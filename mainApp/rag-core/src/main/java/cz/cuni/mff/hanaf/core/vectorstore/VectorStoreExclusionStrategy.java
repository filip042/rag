package cz.cuni.mff.hanaf.core.vectorstore;

public interface VectorStoreExclusionStrategy {
    boolean supports(String provider);
    String getAutoConfigurationClass();
}

package cz.cuni.mff.hanaf.mainapp.providers;

public interface VectorStoreExclusionStrategy {
    boolean supports(String provider);
    String getAutoConfigurationClass();
}

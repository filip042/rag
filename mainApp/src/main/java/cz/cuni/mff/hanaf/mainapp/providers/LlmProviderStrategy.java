package cz.cuni.mff.hanaf.mainapp.providers;

public interface LlmProviderStrategy {
    boolean supports(String provider);
    void apply(LlmProperties properties);
}

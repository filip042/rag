package cz.cuni.mff.hanaf.mainapp.llm;

public interface LlmProviderStrategy {
    boolean supports(String provider);
    void apply(LlmProperties properties);
}


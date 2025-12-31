package cz.cuni.mff.hanaf.core.llm;

import cz.cuni.mff.hanaf.core.config.LlmProperties;

public interface LlmProviderStrategy {
    boolean supports(String provider);
    void apply(LlmProperties properties);
}

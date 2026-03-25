package cz.cuni.mff.hanaf.core.llm;

public interface LlmMethodsFactory {
    boolean supports(String providerName, String modelName);
    LlmMethods create(String modelName);
}

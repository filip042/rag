package cz.cuni.mff.hanaf.core.llm;

public interface LlmMethodsFactory {
    boolean supports(String modelName);
    LlmMethods create(String modelName);
}

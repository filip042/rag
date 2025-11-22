package cz.cuni.mff.hanaf.mainapp.llm;

public interface LlmMethodsFactory {
    boolean supports(String modelName);
    LlmMethods create(String modelName);
}

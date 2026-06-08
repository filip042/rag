package cz.cuni.mff.hanaf.core.llm;

/**
 * Factory interface for dynamically creating {@link LlmMethods} instances.
 */
public interface LlmMethodsFactory {

    /**
     * Checks whether this factory can create an {@link LlmMethods} instance for the given provider and model.
     *
     * @param providerName the name of the LLM provider
     * @param modelName the specific model name
     * @return {@code true} if this factory supports the given combination, {@code false} otherwise
     */
    boolean supports(String providerName, String modelName);

    /**
     * Creates and configures an {@link LlmMethods} instance for the specified model.
     *
     * @param modelName the model name to configure the LLM instance with
     * @return an {@link LlmMethods} instance
     */
    LlmMethods create(String modelName);
}

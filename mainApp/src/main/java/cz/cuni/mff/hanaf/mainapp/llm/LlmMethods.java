package cz.cuni.mff.hanaf.mainapp.llm;

import java.util.Map;

public interface LlmMethods {
    Map<String, Object> prepareAnswer(String answer);
}

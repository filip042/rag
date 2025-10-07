package cz.cuni.mff.hanaf.mainapp.rag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils { // todo move to LLM-specific code, make some function like prepareOutput
    public static String removeThinking(String data) {
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        return matcher.replaceFirst("");
    }
}

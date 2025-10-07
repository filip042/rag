package cz.cuni.mff.hanaf.mainapp.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Qwen3Methods implements LlmMethods {
    @Override
    public Map<String, Object> prepareAnswer(String answer) {
        System.out.println(answer); // todo test remove

        answer = this.removeThinking(answer);

        String[] lines = answer.strip().split("\\R");
        Set<String> sources = new HashSet<>();

        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1].trim();
            if (!lastLine.isEmpty()) {
                Pattern p = Pattern.compile("[^,]+?\\.[A-Za-z0-9]+"); // e.g., "file-name.md"
                Matcher m = p.matcher(lastLine);
                while (m.find()) {
                    sources.add(m.group().trim());
                }
            }
            answer = Arrays.stream(lines)
                    .limit(lines.length - 1)
                    .collect(Collectors.joining("\n"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        result.put("sources", sources);
        result.put("done", true);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
            System.out.println(jsonOutput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String removeThinking(String withThinking) {
        Pattern pattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(withThinking);
        return matcher.replaceFirst("");
    }
}

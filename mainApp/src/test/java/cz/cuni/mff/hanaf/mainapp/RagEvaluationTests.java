package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.mainapp.rag.FileLoader;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("openai")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RagEvaluationTests {

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private RelevancyEvaluator relevancyEvaluator;

    @Autowired
    private FactCheckingEvaluator factCheckingEvaluator;

    private static final Path RESULT_PATH = Path.of("target/rag-eval-results.csv");

    @ParameterizedTest
    @CsvFileSource(resources = "/rag-testcases.csv", numLinesToSkip = 1)
    void evaluateRagResponse(String id, String query, String expected, long workspace, int repetitions) throws Exception {
        Set<String> expectedSet = Set.of(expected.split(";"));
        int relevant = 0;
        int factual = 0;
        for (int i = 0; i < repetitions; i++) {
            Map<String, Object> progress = new HashMap<>();
            fileLoader.ask(query, workspace, progress).join();

            String answer = (String) progress.get("answer");
            List<Document> context = (List<Document>) progress.get("documents");
            Set<String> contextSources = context.stream()
                    .map(document -> (String) document.getMetadata().get("source"))
                    .collect(Collectors.toSet());
            Set<String> tp = expectedSet.stream().filter(contextSources::contains).collect(Collectors.toSet());
            Set<String> fp = contextSources.stream().filter(source -> !expectedSet.contains(source)).collect(Collectors.toSet());
            Set<String> fn = expectedSet.stream().filter(source -> !contextSources.contains(source)).collect(Collectors.toSet());

            EvaluationRequest relevancyReq = new EvaluationRequest(query, context, answer);
            relevant += relevancyEvaluator.evaluate(relevancyReq).isPass() ? 1 : 0;
            factual += factCheckPerDocument(answer, context) ? 1 : 0;
        }

        writeResult(id, query, relevant, factual, repetitions);
    }

    private synchronized void writeResult(String id, String query, int relevant, int factual, int repetitions) throws Exception {
        if (!Files.exists(RESULT_PATH)) {
            Files.createDirectories(RESULT_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH)) {
                writer.write("id,query,relevant,factual\n");
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH, StandardOpenOption.APPEND)) {
            writer.write("%s,%s,%d/%d,%d/%d%n".formatted(id, query.replace(",", " "), relevant, repetitions, factual, repetitions));
        }
    }

    private boolean factCheckPerDocument(String answer, List<Document> context) {
        for (Document doc : context) {
            EvaluationRequest req = new EvaluationRequest(List.of(doc), answer);
            EvaluationResponse resp = factCheckingEvaluator.evaluate(req);
            if (!resp.isPass()) { // maybe print here
                return false;
            }
        }
        return true;
    }
}
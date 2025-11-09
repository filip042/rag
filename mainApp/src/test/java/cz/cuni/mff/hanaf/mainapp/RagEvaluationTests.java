package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.mainapp.rag.FileLoader;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
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
    void evaluateRagResponse(String id, String query, long workspace) throws Exception {
        Map<String, Object> progress = new HashMap<>();
        fileLoader.ask(query, workspace, progress).join();

        String answer = (String) progress.get("answer");
        List<Document> context = fileLoader.searchSimilarDocuments(query, workspace, 5);

        EvaluationRequest req = new EvaluationRequest(query, context, answer);
        boolean relevant = relevancyEvaluator.evaluate(req).isPass();
        boolean factual = factCheckingEvaluator.evaluate(req).isPass();

        writeResult(id, query, relevant, factual);
    }

    private synchronized void writeResult(String id, String query, boolean relevant, boolean factual) throws Exception {
        if (!Files.exists(RESULT_PATH)) {
            Files.createDirectories(RESULT_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH)) {
                writer.write("id,query,relevant,factual\n");
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH, StandardOpenOption.APPEND)) {
            writer.write("%s,%s,%b,%b%n".formatted(id, query.replace(",", " "), relevant, factual));
        }
    }
}
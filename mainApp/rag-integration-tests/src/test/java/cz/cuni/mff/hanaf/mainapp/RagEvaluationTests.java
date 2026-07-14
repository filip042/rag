package cz.cuni.mff.hanaf.mainapp;

import com.opencsv.CSVReader;
import cz.cuni.mff.hanaf.mainapp.config.TestConfig;
import cz.cuni.mff.hanaf.mainapp.rag.RagService;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("openai")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RagEvaluationTests {

    private final RagService ragService;
    private final OllamaApi ollamaApi;
    private final OpenAiApi openAiApi;
    private final RelevancyEvaluator relevancyEvaluator;
    private final FactCheckingEvaluator factCheckingEvaluator;

    private static final Path RESULT_PATH = Path.of("target/chunk-size-results.csv");

    @Autowired
    RagEvaluationTests(RagService ragService, OllamaApi ollamaApi, OpenAiApi openAiApi, RelevancyEvaluator relevancyEvaluator, FactCheckingEvaluator factCheckingEvaluator) {
        this.ragService = ragService;
        this.ollamaApi = ollamaApi;
        this.openAiApi = openAiApi;
        this.relevancyEvaluator = relevancyEvaluator;
        this.factCheckingEvaluator = factCheckingEvaluator;
    }

    void evaluateRagResponse(String id, String query, String expected, long projectId, int repetitions, ChatModel chatModel) throws Exception {
        Set<String> expectedSet = Set.of(expected.split(";"));
        int relevant = 0;
        int factual = 0;
        double precision = 0;
        double recall = 0;
        double f1Score = 0;
        for (int i = 0; i < repetitions; i++) {
            Map<String, Object> progress = new HashMap<>();
            ragService.ask(query, projectId, progress, chatModel).join();

            String answer = (String) progress.get("answer");
            List<Document> context = (List<Document>) progress.get("documents");
            Set<String> contextSources = context.stream()
                    .map(document -> (String) document.getMetadata().get("source"))
                    .collect(Collectors.toSet());
            Set<String> tp = expectedSet.stream().filter(contextSources::contains).collect(Collectors.toSet());
            Set<String> fp = contextSources.stream().filter(source -> !expectedSet.contains(source)).collect(Collectors.toSet());
            Set<String> fn = expectedSet.stream().filter(source -> !contextSources.contains(source)).collect(Collectors.toSet());

            double p = (double) tp.size() / (tp.size() + fp.size() + 1e-9);
            double r = (double) tp.size() / (tp.size() + fn.size() + 1e-9);
            double f1 = 2 * p * r / (p + r + 1e-9);

            precision += p;
            recall += r;
            f1Score += f1;

            EvaluationRequest relevancyReq = new EvaluationRequest(query, context, answer);
            relevant += relevancyEvaluator.evaluate(relevancyReq).isPass() ? 1 : 0;
            factual += factCheckDocuments(answer, context) ? 1 : 0;
        }
        precision /= repetitions;
        recall /= repetitions;
        f1Score /= repetitions;

        writeResult(id, query, relevant, factual, repetitions, precision, recall, f1Score);
    }

    private void runTestCasesForExperiment(String experimentId, ChatModel chatModel, long projectId, int repetitions) throws Exception {

        List<String[]> cases = loadTestCases();

        for (String[] row : cases) {
            String query = row[1];
            String expected = row[2];

            evaluateRagResponse(experimentId, query, expected, projectId, repetitions, chatModel);
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/rag-experiments.csv", numLinesToSkip = 1)
    void runExperiment(String experimentId, String provider, String model, String prompt, double temperature, long projectId, int repetitions) throws Exception {
        if (experimentId.startsWith("#")) {
            return;
        }
        ChatModel chatModel = createChatModel(provider, model, temperature);

        Resource promptResource;
        if (prompt.equals("default")) {
            promptResource = new ClassPathResource("prompts/ask-template.txt");
        } else {
            promptResource = new ClassPathResource("prompts/" + prompt + ".txt");
        }
        ragService.setSystemPrompt(promptResource);

        if (provider.equals("openai")) {
            Thread.sleep(2000);
        }

        runTestCasesForExperiment(experimentId, chatModel, projectId, repetitions);
    }

    private ChatModel createChatModel(String provider, String modelName, double temperature) {
        if (provider.equals("openai")) {
            return OpenAiChatModel.builder()
                    .defaultOptions(OpenAiChatOptions.builder().model(modelName).temperature(temperature).build())
                    .openAiApi(openAiApi)
                    .build();
        } else if(provider.equals("ollama")) {
            return OllamaChatModel.builder()
                    .defaultOptions(OllamaChatOptions.builder().model(modelName).temperature(temperature).numPredict(4096).build())
                    .ollamaApi(ollamaApi)
                    .build();
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    private List<String[]> loadTestCases() throws CsvException, IOException {
        Resource resource = new ClassPathResource("rag-testcases.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            List<String[]> all = reader.readAll();
            return all.subList(1, all.size());
        }
    }

    private synchronized void writeResult(String id, String query, int relevant, int factual, int repetitions, double precision, double recall, double f1) throws Exception {
        if (!Files.exists(RESULT_PATH)) {
            Files.createDirectories(RESULT_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH)) {
                writer.write("id,query,relevant,factual,precision,recall,f1\n");
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(RESULT_PATH, StandardOpenOption.APPEND)) {
            writer.write("%s,%s,%d/%d,%d/%d,%.4f,%.4f,%.4f%n".formatted(id, query.replace(",", " "), relevant, repetitions, factual, repetitions, precision, recall, f1));
        }
    }

    private boolean factCheckDocuments(String answer, List<Document> context) {
        EvaluationRequest req = new EvaluationRequest(context, answer);
        EvaluationResponse resp = factCheckingEvaluator.evaluate(req);
        return resp.isPass();
    }
}
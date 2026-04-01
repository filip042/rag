package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.*;
import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ProjectRepository projectRepository;
    private final QuestionRepository questionRepository;
    private final LlmMethods llmMethods;
    private final Executor llmExecutor;
    private final DocumentLoader documentLoader;

    public RagService(VectorStore vectorStore, ChatModel chatModel, ProjectRepository projectRepository, QuestionRepository questionRepository, LlmMethods llmMethods, @Qualifier("llmExecutor") Executor llmExecutor, DocumentLoader documentLoader) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.projectRepository = projectRepository;
        this.questionRepository = questionRepository;
        this.llmMethods = llmMethods;
        this.llmExecutor = llmExecutor;
        this.documentLoader = documentLoader;
    }

    @Value("classpath:prompts/ask-template.txt")
    private Resource askTemplateResource;

    @Value("classpath:prompts/do-not-know-prompt.txt")
    private Resource doNotKnowPromptResource;

    private final Map<Long, ConcurrentLinkedQueue<String>> finishedFiles = new ConcurrentHashMap<>();
    private final Map<Long, List<Path>> allFilesToIndex = new ConcurrentHashMap<>();

    /**
     * Get an answer to a question from the LLM using the documents in the workspace,
     * using the default chat model. See {@link #ask(String, long, Map, ChatModel)} for full details.
     *
     * @param query     The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @param progress  The map to be updated with task progress and results
     * @return A CompletableFuture that completes when the answer has been written to {@code progress}
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress) {
        return ask(query, workSpace, progress, null);
    }

    /**
     * Get an answer to a question from the LLM using the documents in the workspace.
     * Results are written into {@code progress} under "answer", "sources", "documents",
     * and "status" (set to "done" on completion).
     *
     * @param query     The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @param progress  The map to be updated with task progress and results
     * @param chatModel The chat model to use, or null to use the default
     * @return A CompletableFuture that completes when the answer has been written to {@code progress}
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress, ChatModel chatModel) {
        if (chatModel == null) {
            chatModel = this.chatModel;
        }
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        return CompletableFuture.supplyAsync(() -> {
            SearchRequest request = buildSearchRequest(query, workSpace, progress);
            List<Document> relevant = filterRelevantDocuments(request, query, progress);
            VerifyingQuestionAnswerAdvisor qaAdvisor = buildAdvisor(request, relevant);

            ChatClientResponse clientResponse = chatClient.prompt(query)
                    .advisors(qaAdvisor)
                    .call()
                    .chatClientResponse();

            String answer = Optional.ofNullable(clientResponse.chatResponse())
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(null);

            List<Document> documents = qaAdvisor.getVerifiedDocuments();

            Set<String> sources = documents.stream()
                    .map(this::extractDocumentName)
                    .collect(Collectors.toSet());

            String formattedAnswer = llmMethods.prepareAnswer(answer);

            progress.put("answer", formattedAnswer);
            progress.put("sources", sources);
            progress.put("documents", documents);
            progress.put("status", "done");

            saveQuestion(query, formattedAnswer, sources, workSpace);

            return null;
        }, llmExecutor);
    }

    /**
     * Asynchronously indexes the given files into the given workspace.
     * Files whose hash matches an already-indexed version are skipped; files
     * whose hash has changed are re-indexed. Cleans up temporary storage once done.
     *
     * @param files     The files to be indexed. Files that are empty or don't have a name are ignored
     * @param workspace The id of the workspace to add the files to
     */
    public void addDocuments(MultipartFile[] files, long workspace) {
        if (files == null || files.length == 0) {
            return;
        }

        try {
            Instant indexingStartTime = Instant.now();
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(workspace, finishedQueue);

            Project project = projectRepository.getReferenceById(workspace);
            Map<String, FileInfo> existingFiles = new ConcurrentHashMap<>(Optional.ofNullable(project.getFiles()).orElse(Map.of()));

            Path tempDir = Files.createTempDirectory("uploads_" + workspace);
            List<Path> toIndex = saveUploadedFiles(files, tempDir);
            allFilesToIndex.put(workspace, toIndex);

            removeStaleDocuments(toIndex, existingFiles, workspace);

            List<CompletableFuture<Void>> futures = toIndex.stream()
                    .filter(f -> {
                        if (isAlreadyIndexed(f, existingFiles)) {
                            finishedQueue.add(f.getFileName().toString());
                            return false;
                        }
                        return true;
                    })
                    .map(f -> CompletableFuture.runAsync(
                            () -> indexFile(f, workspace, existingFiles, indexingStartTime, finishedQueue),
                            llmExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> onIndexingComplete(project, existingFiles, tempDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the chunks in the given workspace from the given file.
     * Spring AI's built-in delete method for Filter Expressions does text search instead of exact-match, leading to false positives, e.g., E._R._Eddison.txt being returned for J._R._R._Tolkien.txt
     * Deleting by UUID bypasses this
     * @param workspace The workspace to delete from
     * @param fileId The UUID of the file whose chunks should be deleted
     */
    private void deleteDocumentsForFile(long workspace, String fileId) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.and(
                expressionBuilder.eq("workSpace", workspace),
                expressionBuilder.eq("fileId", fileId)
        ).build();
        vectorStore.delete(filterExpression);
    }

    /**
     * Returns the status and list of files processed for the given workspace.
     *
     * @param workspace The id of the workspace to check
     * @return A map with two key-value pairs:
     *         - "totalFiles": The number of documents that are being indexed
     *         - "finishedFiles": A list of file paths that have been successfully processed
     */
    public Map<String, Object> allAdded(long workspace) {
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(workspace);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();
        int total = (allFilesToIndex.get(workspace) != null) ? allFilesToIndex.get(workspace).size() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalFiles", total);
        result.put("finishedFiles", finishedList);

        return result;
    }

    /**
     * Removes all documents in the given workspace from the database
     *
     * @param workspace The id of the workspace that is being deleted
     */
    public void deleteWorkspace(long workspace) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("workSpace", workspace).build();
        vectorStore.delete(filterExpression);
        System.out.println("Deleted workspace " + workspace);
    }

    /**
     * Overrides the system prompt template used when answering questions.
     * Intended for use in tests.
     *
     * @param promptResource The resource containing the new prompt template
     */
    public void setSystemPrompt(Resource promptResource) {
        this.askTemplateResource = promptResource;
    }

    private String extractDocumentName(Document document) {
        Pattern pattern = Pattern.compile("<chunk source=\"([^\"]+)\">\\s*(.*?)\\s*</chunk>", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(Objects.requireNonNullElse(document.getText(), ""));
        String fileName = "";
        if (matcher.find()) {
            fileName = matcher.group(1);
        }

        return fileName;
    }

    private String computeHash(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return DigestUtils.md5DigestAsHex(is);
        }
    }

    private SearchRequest buildSearchRequest(String query, long workSpace, Map<String, Object> progress) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("workSpace", workSpace).build();
        int size = 5; // todo should probably be constant
        progress.put("total", size);
        return SearchRequest.builder().query(query).filterExpression(filterExpression).topK(size).build();
    }

    private List<Document> filterRelevantDocuments(SearchRequest request, String query, Map<String, Object> progress) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean verified = new AtomicBoolean(false);
        progress.put("checked", count);
        progress.put("checked_all", verified);

        List<Document> relevant = vectorStore.similaritySearch(request).stream()
                .filter(doc -> {
                    boolean isRelevant = llmMethods.verifySource(doc.getText(), query);
                    count.incrementAndGet();
                    return isRelevant;
                })
                .toList();

        verified.set(true);
        return relevant;
    }

    private VerifyingQuestionAnswerAdvisor buildAdvisor(SearchRequest request, List<Document> relevant) {
        try {
            return VerifyingQuestionAnswerAdvisor.builder(vectorStore)
                    .promptTemplate(new SystemPromptTemplate(askTemplateResource))
                    .doNotKnowPrompt(doNotKnowPromptResource.getContentAsString(StandardCharsets.UTF_8))
                    .searchRequest(request)
                    .documents(relevant)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveQuestion(String query, String answer, Set<String> sources, long workSpace) {
        projectRepository.findById(workSpace).ifPresent(project -> {
            Question question = new Question();
            question.setQuestion(query);
            question.setAnswer(answer);
            question.setAnswerTime(Instant.now());
            question.setSources(sources);
            question.setProject(project);
            questionRepository.save(question);
        });
    }

    private List<Path> saveUploadedFiles(MultipartFile[] files, Path tempDir) throws IOException {
        List<Path> toIndex = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty() && file.getOriginalFilename() != null) {
                Path filePath = tempDir.resolve(file.getOriginalFilename());
                Files.createDirectories(filePath.getParent());
                file.transferTo(filePath.toFile());
                toIndex.add(filePath);
            }
        }
        return toIndex;
    }

    private void removeStaleDocuments(List<Path> toIndex, Map<String, FileInfo> existingFiles, long workspace) {
        Set<String> filesToRemove = toIndex.stream()
                .filter(f -> existingFiles.containsKey(f.getFileName().toString()) && !isAlreadyIndexed(f, existingFiles))
                .map(f -> existingFiles.get(f.getFileName().toString()).getFileId())
                .collect(Collectors.toSet());

        if (!filesToRemove.isEmpty()) {
            System.out.println("Removing old versions of " + filesToRemove.size() + " files");
            filesToRemove.forEach(fileId -> deleteDocumentsForFile(workspace, fileId));
        }
    }

    private boolean isAlreadyIndexed(Path f, Map<String, FileInfo> existingFiles) {
        String fileName = f.getFileName().toString();
        if (!existingFiles.containsKey(fileName)) {
            return false;
        }
        try {
            return computeHash(f).equals(existingFiles.get(fileName).getHash());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void indexFile(Path f, long workspace, Map<String, FileInfo> existingFiles, Instant indexingStartTime, ConcurrentLinkedQueue<String> finishedQueue) {
        String fileName = f.getFileName().toString();
        String fileId = existingFiles.containsKey(fileName) ? existingFiles.get(fileName).getFileId() : UUID.randomUUID().toString();

        try {
            documentLoader.load(f, workspace, indexingStartTime, fileId);
            System.out.println("Finished processing: " + f);
        } catch (Exception e) { // todo
            System.err.println("Failed processing " + f + ": " + e.getMessage());
        }

        try {
            existingFiles.put(fileName, new FileInfo(fileId, computeHash(f)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        finishedQueue.add(fileName);
    }

    private void onIndexingComplete(Project project, Map<String, FileInfo> existingFiles, Path tempDir) {
        project.setFiles(new HashMap<>(existingFiles));
        projectRepository.save(project);
        cleanUpTempDir(tempDir);
    }

    private void cleanUpTempDir(Path tempDir) {
        try (Stream<Path> paths = Files.walk(tempDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete temp file: " + path);
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to clean up temp directory: " + e.getMessage());
        }
    }
}

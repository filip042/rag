package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.*;
import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import cz.cuni.mff.hanaf.mainapp.rag.dto.IndexStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Service handling document indexing and RAG queries.
 * Manages asynchronous file indexing into a vector store and answering natural-language
 * questions against indexed project documents using an LLM.
 */
@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ProjectRepository projectRepository;
    private final QuestionRepository questionRepository;
    private final LlmMethods llmMethods;
    private final Executor llmExecutor;
    private final DocumentLoader documentLoader;
    private final QueryProperties queryProperties;
    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    /**
     * Creates a new {@code RagService}.
     *
     * @param vectorStore the vector store used for document storage and similarity search
     * @param chatModel the default chat model used for generating answers
     * @param projectRepository repository for loading and persisting projects
     * @param questionRepository repository for persisting answered questions
     * @param llmMethods LLM methods for answer preparation and source verification
     * @param llmExecutor the executor for async LLM and indexing
     * @param documentLoader the loader used to parse and index documents
     * @param queryProperties configuration properties for query handling
     */
    public RagService(VectorStore vectorStore, ChatModel chatModel, ProjectRepository projectRepository, QuestionRepository questionRepository, LlmMethods llmMethods, @Qualifier("llmExecutor") Executor llmExecutor, DocumentLoader documentLoader, QueryProperties queryProperties) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.projectRepository = projectRepository;
        this.questionRepository = questionRepository;
        this.llmMethods = llmMethods;
        this.llmExecutor = llmExecutor;
        this.documentLoader = documentLoader;
        this.queryProperties = queryProperties;
    }

    @Value("classpath:prompts/ask-template.txt")
    private Resource askTemplateResource;

    @Value("classpath:prompts/do-not-know-prompt.txt")
    private Resource doNotKnowPromptResource;

    private final Map<Long, ConcurrentLinkedQueue<String>> finishedFiles = new ConcurrentHashMap<>();
    private final Map<Long, List<Path>> allFilesToIndex = new ConcurrentHashMap<>();

    /**
     * Gets an answer to a question from the LLM using the documents in the project,
     * using the default chat model. See {@link #ask(String, long, Map, ChatModel)} for full details.
     *
     * @param query the query to be answered
     * @param projectId the id of the project with the source documents
     * @param progress the map to be updated with task progress and results
     * @return a CompletableFuture that completes when the answer has been written to {@code progress}
     */
    public CompletableFuture<Void> ask(String query, long projectId, Map<String, Object> progress) {
        return ask(query, projectId, progress, null);
    }

    /**
     * Gets an answer to a question from the LLM using the documents in the project.
     * Results are written into {@code progress} under "answer", "sources", "documents",
     * and "status" (set to "done" on completion).
     *
     * @param query the query to be answered
     * @param projectId the id of the project with the source documents
     * @param progress the map to be updated with task progress and results
     * @param chatModel the chat model to use, or {@code null} to use the default
     * @return a CompletableFuture that completes when the answer has been written to {@code progress}
     */
    public CompletableFuture<Void> ask(String query, long projectId, Map<String, Object> progress, ChatModel chatModel) {
        if (chatModel == null) {
            chatModel = this.chatModel;
        }
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        return CompletableFuture.supplyAsync(() -> {
            SearchRequest request = buildSearchRequest(query, projectId, progress);
            List<Document> relevant = filterRelevantDocuments(request, query, progress);
            VerifyingQuestionAnswerAdvisor qaAdvisor = buildAdvisor(request, relevant);

            ChatClientResponse clientResponse = chatClient.prompt(query)
                    .advisors(qaAdvisor)
                    .call()
                    .chatClientResponse();

            logger.debug("Chat client response: {}", clientResponse);

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

            saveQuestion(query, formattedAnswer, sources, projectId);

            return null;
        }, llmExecutor);
    }

    /**
     * Asynchronously indexes the given files into the given project.
     * Files whose hash matches an already indexed version are skipped.
     * Files whose hash has changed are reindexed. Cleans up temporary storage once done.
     *
     * @param files the files to be indexed. Files that are empty or don't have a name are ignored
     * @param projectId the id of the project to add the files to
     */
    public void addDocuments(MultipartFile[] files, long projectId) {
        if (files == null || files.length == 0) {
            return;
        }

        try {
            Instant indexingStartTime = Instant.now();
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(projectId, finishedQueue);

            Project project = projectRepository.getReferenceById(projectId);
            Map<String, FileInfo> existingFiles = new ConcurrentHashMap<>(Optional.ofNullable(project.getFiles()).orElse(Map.of()));

            Path tempDir = Files.createTempDirectory("uploads_" + projectId);
            List<Path> toIndex = saveUploadedFiles(files, tempDir);
            allFilesToIndex.put(projectId, toIndex);

            removeStaleDocuments(toIndex, existingFiles, projectId);

            List<CompletableFuture<Void>> futures = toIndex.stream()
                    .filter(f -> {
                        if (isAlreadyIndexed(f, existingFiles)) {
                            finishedQueue.add(f.getFileName().toString());
                            return false;
                        }
                        return true;
                    })
                    .map(f -> CompletableFuture.runAsync(
                            () -> indexFile(f, projectId, existingFiles, indexingStartTime, finishedQueue),
                            llmExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> onIndexingComplete(project, existingFiles, tempDir))
                    .exceptionally(ex -> {
                        logger.error("Failed to finalize indexing for project {}", projectId, ex);
                        return null;
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the chunks in the given project from the given file.
     * Spring AI's built-in delete method for Filter Expressions does text search instead of exact-match in older versions, leading to false positives, e.g., E._R._Eddison.txt being returned for J._R._R._Tolkien.txt
     * Deleting by UUID bypasses this.
     *
     * @param projectId the id of the project to delete from
     * @param fileId the UUID of the file whose chunks should be deleted
     */
    private void deleteDocumentsForFile(long projectId, String fileId) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.and(
                expressionBuilder.eq("project", projectId),
                expressionBuilder.eq("fileId", fileId)
        ).build();
        vectorStore.delete(filterExpression);
    }

    /**
     * Returns the status and list of files processed for the given project.
     *
     * @param projectId the id of the project to check
     * @return a {@link IndexStatusResponse} record
     */
    public IndexStatusResponse allAdded(long projectId) {
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(projectId);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();
        int total = (allFilesToIndex.get(projectId) != null) ? allFilesToIndex.get(projectId).size() : 0;

        return new IndexStatusResponse(total, finishedList);
    }

    /**
     * Removes all documents in the given project from the database.
     *
     * @param projectId the id of the project that is being deleted
     */
    public void deleteProject(long projectId) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("project", projectId).build();
        vectorStore.delete(filterExpression);
        logger.info("Deleted project {}", projectId);
    }

    /**
     * Overrides the system prompt template used when answering questions.
     * Intended for use in tests.
     *
     * @param promptResource the resource containing the new prompt template
     */
    public void setSystemPrompt(Resource promptResource) {
        this.askTemplateResource = promptResource;
    }

    private String extractDocumentName(Document document) {
        return (String) document.getMetadata().getOrDefault("source", "");
    }

    private String computeHash(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return DigestUtils.md5DigestAsHex(is);
        }
    }

    private SearchRequest buildSearchRequest(String query, long projectId, Map<String, Object> progress) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("project", projectId).build();
        int size = 5; // todo should probably be constant
        progress.put("total", size);
        int maxQueryLength = queryProperties.getMaxQueryLength();
        if (query.length() > maxQueryLength) {
            query = query.substring(0, maxQueryLength);
        }
        query = queryProperties.getSearchPrefix() + query;
        return SearchRequest.builder().query(query).filterExpression(filterExpression).topK(size).build();
    }

    private List<Document> filterRelevantDocuments(SearchRequest request, String query, Map<String, Object> progress) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean verified = new AtomicBoolean(false);
        progress.put("checked", count);
        progress.put("checked_all", verified);

        logger.debug("Search request query: {}", request.getQuery());

        List<Document> relevant = vectorStore.similaritySearch(request).stream()
                .filter(doc -> {
                    logger.trace("Evaluating document: {}", doc);
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
            return VerifyingQuestionAnswerAdvisor.builder()
                    .promptTemplate(new SystemPromptTemplate(askTemplateResource))
                    .doNotKnowPrompt(doNotKnowPromptResource.getContentAsString(StandardCharsets.UTF_8))
                    .documents(relevant)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveQuestion(String query, String answer, Set<String> sources, long projectId) {
        projectRepository.findById(projectId).ifPresent(project -> {
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

    private void removeStaleDocuments(List<Path> toIndex, Map<String, FileInfo> existingFiles, long projectId) {
        Set<String> filesToRemove = toIndex.stream()
                .filter(f -> existingFiles.containsKey(f.getFileName().toString()) && !isAlreadyIndexed(f, existingFiles))
                .map(f -> existingFiles.get(f.getFileName().toString()).getFileId())
                .collect(Collectors.toSet());

        if (!filesToRemove.isEmpty()) {
            logger.info("Removing old versions of {} files", filesToRemove.size());
            filesToRemove.forEach(fileId -> deleteDocumentsForFile(projectId, fileId));
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

    private void indexFile(Path f, long projectId, Map<String, FileInfo> existingFiles, Instant indexingStartTime, ConcurrentLinkedQueue<String> finishedQueue) {
        String fileName = f.getFileName().toString();
        String fileId = existingFiles.containsKey(fileName) ? existingFiles.get(fileName).getFileId() : UUID.randomUUID().toString();

        try {
            documentLoader.load(f, projectId, indexingStartTime, fileId);
            logger.debug("Finished processing: {}", f);
        } catch (Exception e) { // todo
            logger.error("Failed processing {}", f, e);
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
                    logger.warn("Failed to delete temp file: {}", path);
                }
            });
        } catch (IOException e) {
            logger.warn("Failed to clean up temp directory: {}", tempDir, e);
        }
    }
}

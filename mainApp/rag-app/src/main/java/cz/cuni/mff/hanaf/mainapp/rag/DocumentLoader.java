package cz.cuni.mff.hanaf.mainapp.rag;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import cz.cuni.mff.hanaf.core.parser.DocumentParserStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DocumentLoader{
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final List<DocumentParserStrategy> parserStrategies;
    private final SplitterProperties splitterProperties;

    @Value("classpath:/prompts/add-context-template.txt")
    private Resource systemResource;

    @Value("classpath:prompts/summarize-template.txt")
    private Resource summarizeResource;

    public DocumentLoader(VectorStore vectorStore, ChatModel chatModel, List<DocumentParserStrategy> parserStrategies, SplitterProperties splitterProperties) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.parserStrategies = parserStrategies;
        this.splitterProperties = splitterProperties;
    }

    /**
     * Adds the document from the path to the database
     */
    protected void load(Path f, long workspace, Instant finalThisTime, String fileId) {
        String fileName = f.getFileName().toString();
        String p = f.toString();

        List<Document> splitDocuments = readAndSplitDocument(p);
        List<Document> documentsWithSource = prepareDocumentsWithSource(fileName, fileId, workspace, finalThisTime, splitDocuments);

        if (!documentsWithSource.isEmpty()) {
            vectorStore.add(documentsWithSource);
        } else {
            System.out.println(fileName + " is empty");
        }
    }

    /**
     * Splits the document on the given path into chunks
     *
     * @param filePath The path to the given document
     * @return A list of chunks created from the given document
     */
    private List<Document> readAndSplitDocument(String filePath) {
        OverlapTextSplitter splitter = new OverlapTextSplitter(
                splitterProperties.getChunkSize(),
                splitterProperties.getMinChunkSizeChars(),
                splitterProperties.getMinChunkLengthToEmbed(),
                splitterProperties.getMaxNumChunks(),
                splitterProperties.isKeepSeparator(),
                splitterProperties.getOverlapTokens()
        );
        return parserStrategies.stream()
                .filter(p -> p.supports(filePath))
                .findFirst()
                .map(p -> splitter.apply(p.read(filePath)))
                .orElseGet(() -> {
                    System.out.println("No parser found for: " + filePath);
                    return new ArrayList<>();
                });
    }

    /**
     * Adds an xml source tag and metadata for each chunk
     *
     * @param fileName The name of the chunk source file
     * @param workspace The current workspace
     * @param processingTime The current time
     * @param splitDocuments A list of chunks created by splitting documents
     * @return A list of modified Documents
     */
    private List<Document> prepareDocumentsWithSource(String fileName, String fileId, long workspace, Instant processingTime, List<Document> splitDocuments) {
        return splitDocuments.stream()
                .map(document -> {
                    String textWithSource = "search_document: " + document.getText();

                    Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                    metadata.put("workSpace", workspace);
                    metadata.put("lastReadTime", processingTime.getEpochSecond());
                    metadata.put("source", fileName);
                    metadata.put("fileId", fileId);

                    return Document.builder()
                            .text(textWithSource)
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Adds context to the current chunk for indexing using information from the previous and next ones
     *
     * @param previous The previous chunk
     * @param current The current chunk
     * @param next The next chunk
     * @return The current document with context added
     */
    private Document addContext(Document previous, Document current, Document next) { // todo add removeThinking here
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemResource);

        String prompt = promptTemplate.render(Map.of(
                "previous", Optional.ofNullable(previous).map(Document::getText).orElse("No previous document exists."),
                "current", Optional.ofNullable(current).map(Document::getText).orElse("This document doesn't exist"),
                "next", Optional.ofNullable(next).map(Document::getText).orElse("No next document exists.")));
        return new Document(chatModel.call(prompt)); // todo check if works
    }

    /**
     * Summarizes the given document using an LLM so it can be added to indexed chunks as context
     *
     * @param document The document being summarized
     * @return The summarized document
     */
    private String summarizeDocument(Document document) { // todo check if using systemMessage is better
        // todo might be better to have in LLM-specific class
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(summarizeResource);

        assert document.getText() != null;
        String prompt = promptTemplate.render(Map.of("document", document.getText()));
        return chatModel.call(prompt);
    }
}


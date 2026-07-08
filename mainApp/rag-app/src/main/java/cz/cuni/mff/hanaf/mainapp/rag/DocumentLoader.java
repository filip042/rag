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

/**
 * Loads documents from into the vector store by parsing, splitting,
 * and attaching metadata to each chunk before indexing.
 */
@Component
public class DocumentLoader{
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final List<DocumentParserStrategy> parserStrategies;
    private final SplitterProperties splitterProperties;
    private final QueryProperties queryProperties;

    @Value("classpath:/prompts/add-context-template.txt")
    private Resource systemResource;

    @Value("classpath:prompts/summarize-template.txt")
    private Resource summarizeResource;

    /**
     * Creates a new {@code DocumentLoader} with the required dependencies.
     *
     * @param vectorStore the vector store to index documents into
     * @param chatModel the chat model used for context enrichment and summarization
     * @param parserStrategies the list of available document parser strategies
     * @param splitterProperties configuration properties for the text splitter
     */
    public DocumentLoader(VectorStore vectorStore, ChatModel chatModel, List<DocumentParserStrategy> parserStrategies, SplitterProperties splitterProperties, QueryProperties queryProperties) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.parserStrategies = parserStrategies;
        this.splitterProperties = splitterProperties;
        this.queryProperties = queryProperties;
    }

    /**
     * Parses, splits, and indexes the file at the given path into the vector store.
     * Skips indexing if the file is empty.
     *
     * @param f the path to the file to index
     * @param project the id of the project to index the file into
     * @param finalThisTime the time at which indexing started, used to tag chunks
     * @param fileId the unique identifier to associate with this file's chunks
     */
    protected void load(Path f, long project, Instant finalThisTime, String fileId) {
        String fileName = f.getFileName().toString();
        String p = f.toString();

        List<Document> splitDocuments = readAndSplitDocument(p);
        List<Document> documentsWithSource = prepareDocumentsWithSource(fileName, fileId, project, finalThisTime, splitDocuments);

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
     * Formats each chunk and attaches project, source, and file ids, and time as metadata.
     *
     * @param fileName the name of the source file
     * @param fileId the unique identifier of the source file
     * @param project the id of the project the file belongs to
     * @param processingTime the time at which the file was processed
     * @param splitDocuments the list of chunks to process
     * @return a list of documents with source metadata attached
     */
    private List<Document> prepareDocumentsWithSource(String fileName, String fileId, long project, Instant processingTime, List<Document> splitDocuments) {
        return splitDocuments.stream()
                .map(document -> {
                    String textWithSource = queryProperties.getDocumentPrefix() + document.getText();

                    Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                    metadata.put("project", project);
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
     * Summarizes the given document using an LLM so it can be added to indexed chunks as context.
     *
     * @param document The document to summarize
     * @return the summarized document
     */
    private String summarizeDocument(Document document) { // todo check if using systemMessage is better
        // todo might be better to have in LLM-specific class
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(summarizeResource);

        assert document.getText() != null;
        String prompt = promptTemplate.render(Map.of("document", document.getText()));
        return chatModel.call(prompt);
    }
}


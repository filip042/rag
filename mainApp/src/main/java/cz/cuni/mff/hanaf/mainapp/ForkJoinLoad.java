package cz.cuni.mff.hanaf.mainapp;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class ForkJoinLoad extends RecursiveTask<Void>{
    private final Path f;
    private final String path;
    private final Instant finalThisTime;
    private final MarkdownDocumentReaderConfig config;
    private final SynchronizedVectorStore vectorStore;
    private final ChatModel chatModel;

    private final List<String> formats = new ArrayList<>(List.of(".txt", ".html", ".pdf"));

    public ForkJoinLoad(Path f, String path, Instant finalThisTime, MarkdownDocumentReaderConfig config, VectorStore vectorStore, ChatModel chatModel) {
        this.f = f;
        this.path = path;
        this.finalThisTime = finalThisTime;
        this.config = config;
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
    }

    @Override
    protected Void compute() {
        String fileName = f.getFileName().toString();
        String p = f.toString();
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 100, 5, 10000, false);
        if (p.endsWith(".md")) {
            MarkdownDocumentReader reader = new MarkdownDocumentReader("file:" + p, config);
            vectorStore.add(splitter.apply(reader.get()));
        } else if (formats.stream().anyMatch(p::endsWith)) {
            TikaDocumentReader reader = new TikaDocumentReader("file:" + p);
            List<Document> splitDocuments = splitter.apply(reader.get());
            List<Document> modifiedDocuments = new ArrayList<>(splitDocuments.size());
//            for (int i = 0; i < splitDocuments.size(); i++) { // todo calls to llm time out
//                Document previous = (i > 0) ? splitDocuments.get(i - 1) : null;
//                Document current = splitDocuments.get(i);
//                Document next = (i < splitDocuments.size() - 1) ? splitDocuments.get(i + 1) : null;
//
//                Document result = addContext(previous, current, next);
//                System.out.println("Working");
//                modifiedDocuments.add(result);
//            }
            for (Document document : splitDocuments) {
                document.getMetadata().put("workSpace", path);
                document.getMetadata().put("lastReadTime", finalThisTime.getEpochSecond());
            }
            vectorStore.add(modifiedDocuments);
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filterExpression = b.and(b.and(b.eq("workSpace", path), b.lt("lastReadTime", finalThisTime.getEpochSecond())), b.eq("source", fileName)).build();
        vectorStore.delete(filterExpression);
        return null;
    }

    private Document addContext(Document previous, Document current, Document next) { // todo do in the background, not here
        PromptTemplate promptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
            .template("""
        Take the following three texts, and update the second one to make sense without the first and last.
        Make as few changes as possible in order to preserve the original meaning.
        Don't include any introductory or closing statements in your answer, only the modified second text.
        
        Text 1:
        <previous>
        
        Text 2 (The one to modify)
        <current>
        
        Text 3:
        <next>
        """)
            .build();

        String prompt = promptTemplate.render(Map.of(
                "previous", Optional.ofNullable(previous).map(Document::getText).orElse("No previous document exists."),
                "current", Optional.ofNullable(current).map(Document::getText).orElse("This document doesn't exist"),
                "next", Optional.ofNullable(next).map(Document::getText).orElse("No next document exists.")));
        return new Document(Utils.removeThinking(chatModel.call(prompt))); // todo check if works
    }
}


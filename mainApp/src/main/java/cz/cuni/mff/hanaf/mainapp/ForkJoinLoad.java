package cz.cuni.mff.hanaf.mainapp;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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

    private final List<String> formats = new ArrayList<>(List.of(".txt", ".html", ".pdf"));

    public ForkJoinLoad(Path f, String path, Instant finalThisTime, MarkdownDocumentReaderConfig config, VectorStore vectorStore) {
        this.f = f;
        this.path = path;
        this.finalThisTime = finalThisTime;
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.config = config;
    }

    @Override
    protected Void compute() {
        String fileName = f.getFileName().toString();
        String p = f.toString();
        TokenTextSplitter splitter = new TokenTextSplitter(100, 100, 5, 10000, false);
        if (p.endsWith(".md")) {
            MarkdownDocumentReader reader = new MarkdownDocumentReader("file:" + p, config);
            vectorStore.add(splitter.apply(reader.get()));
        } else if (formats.stream().anyMatch(p::endsWith)) {
            TikaDocumentReader reader = new TikaDocumentReader("file:" + p);
            List<Document> splitDocuments = splitter.apply(reader.get()); // todo replace splitter with LLM
            for (Document document : splitDocuments) {
                document.getMetadata().put("workSpace", path);
                document.getMetadata().put("lastReadTime", finalThisTime.getEpochSecond());
            }
            vectorStore.add(splitDocuments);
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filterExpression = b.and(b.and(b.eq("workSpace", path), b.lt("lastReadTime", finalThisTime.getEpochSecond())), b.eq("source", fileName)).build();
        vectorStore.delete(filterExpression);
        return null;
    }

    private Document addContext() {
        return new Document("");
    }
}


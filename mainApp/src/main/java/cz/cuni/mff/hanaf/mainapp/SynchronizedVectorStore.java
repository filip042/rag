package cz.cuni.mff.hanaf.mainapp;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.lang.NonNull; // todo not sure about this
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public class SynchronizedVectorStore implements VectorStore  {
    private final VectorStore vectorStore;

    SynchronizedVectorStore (VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public synchronized void add(@NonNull List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public synchronized void delete(@NonNull List<String> idList) {
        vectorStore.delete(idList);
    }

    @Override
    public synchronized void delete(@NonNull Filter.Expression filterExpression) {
        vectorStore.delete(filterExpression);
    }

    @Override
    public synchronized void delete(@NonNull String filterExpression) {
        vectorStore.delete(filterExpression);
    }

    @Nullable
    @Override
    public List<Document> similaritySearch(@Nullable SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }

    @Nullable
    @Override
    public List<Document> similaritySearch(@Nullable String query) {
        return vectorStore.similaritySearch(query);
    }

    @NonNull
    @Override
    public String getName() {
        return vectorStore.getName();
    }

    @NonNull
    @Override
    public <T> Optional<T> getNativeClient() {
        return vectorStore.getNativeClient();
    }
}

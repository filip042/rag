package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedVectorStore implements VectorStore {
    private final VectorStore vectorStore;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public SynchronizedVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void add(@NonNull List<Document> documents) {
        lock.writeLock().lock();
        try {
            vectorStore.add(documents);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(@NonNull List<String> idList) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(idList);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(@NonNull Filter.Expression filterExpression) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(filterExpression);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(@NonNull String filterExpression) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(filterExpression);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    @Override
    public List<Document> similaritySearch(@Nullable SearchRequest request) {
        lock.readLock().lock();
        try {
            return vectorStore.similaritySearch(request);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    @Override
    public List<Document> similaritySearch(@Nullable String query) {
        lock.readLock().lock();
        try {
            return vectorStore.similaritySearch(query);
        } finally {
            lock.readLock().unlock();
        }
    }

    @NonNull
    @Override
    public String getName() {
        // getName is typically safe to call without locking
        return vectorStore.getName();
    }

    @NonNull
    @Override
    public <T> Optional<T> getNativeClient() {
        // This is also typically safe
        return vectorStore.getNativeClient();
    }
}
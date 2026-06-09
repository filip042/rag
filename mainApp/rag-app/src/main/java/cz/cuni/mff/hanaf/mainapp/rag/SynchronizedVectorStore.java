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

/**
 * Thread-safe decorator for {@link VectorStore} that serializes writes and allows concurrent reads.
 */
public class SynchronizedVectorStore implements VectorStore {
    private final VectorStore vectorStore;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new {@code SynchronizedVectorStore} wrapping the given vector store.
     *
     * @param vectorStore the vector store to wrap
     */
    public SynchronizedVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(@NonNull List<Document> documents) {
        lock.writeLock().lock();
        try {
            vectorStore.add(documents);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(@NonNull List<String> idList) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(idList);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(@NonNull Filter.Expression filterExpression) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(filterExpression);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(@NonNull String filterExpression) {
        lock.writeLock().lock();
        try {
            vectorStore.delete(filterExpression);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns the name of the underlying vector store.
     * Does not use a lock since this operation is stateless.
     *
     * @return the name of the underlying vector store
     */
    @NonNull
    @Override
    public String getName() {
        return vectorStore.getName();
    }

    /**
     * Returns the native client of the underlying vector store, if available.
     * Does not use a lock since this operation is stateless.
     *
     * @return the native client of the underlying vector store
     */
    @NonNull
    @Override
    public <T> Optional<T> getNativeClient() {
        return vectorStore.getNativeClient();
    }
}

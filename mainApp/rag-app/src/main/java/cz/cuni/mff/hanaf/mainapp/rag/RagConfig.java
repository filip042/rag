package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures RAG-related beans.
 */
@Configuration
public class RagConfig {

    /**
     * Creates and registers a {@link SynchronizedVectorStore} bean.
     *
     * @param delegate the VectorStore instance to be wrapped by the SynchronizedVectorStore
     * @return a thread-safe SynchronizedVectorStore wrapping the specified VectorStore
     */
    @Bean
    public SynchronizedVectorStore synchronizedVectorStore(VectorStore delegate) {
        return new SynchronizedVectorStore(delegate);
    }
}
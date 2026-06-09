package cz.cuni.mff.hanaf.mainapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration properties for the executor beans.
 */
@Configuration
public class ExecutorConfig {

    /**
     * Returns a {@link Executor} bean for use during file upload.
     *
     * @return the executor
     */
    @Bean(name = "llmExecutor")
    public Executor llmExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
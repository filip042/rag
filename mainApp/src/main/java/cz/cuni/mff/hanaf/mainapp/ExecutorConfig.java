package cz.cuni.mff.hanaf.mainapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(name = "llmExecutor")
    public Executor llmExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
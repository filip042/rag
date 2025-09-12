package cz.cuni.mff.hanaf.mainapp.rag;

import jakarta.annotation.PostConstruct;

import java.util.concurrent.*;

public class LlmQueueService {
    private final BlockingQueue<Runnable> summarizationQueue = new LinkedBlockingQueue<>();
    private final ExecutorService summarizationExecutor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        summarizationExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Runnable job = summarizationQueue.take();
                    job.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public CompletableFuture<Void> enqueue(Runnable job) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        summarizationQueue.add(() -> {
            try {
                job.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}

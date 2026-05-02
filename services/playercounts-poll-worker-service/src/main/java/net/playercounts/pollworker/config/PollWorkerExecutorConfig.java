package net.playercounts.pollworker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PollWorkerExecutorConfig {

    @Value("${poll-worker.thread-pool-size}")
    private int threadPoolSize;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService pollWorkerExecutor() {
        return Executors.newFixedThreadPool(threadPoolSize);
    }

}
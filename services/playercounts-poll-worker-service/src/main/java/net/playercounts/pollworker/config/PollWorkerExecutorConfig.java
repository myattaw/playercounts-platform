package net.playercounts.pollworker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PollWorkerExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService pollWorkerExecutor() {
        return Executors.newFixedThreadPool(25);
    }

}
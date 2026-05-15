package net.playercounts.pollworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = "net.playercounts.models.entity")
@EnableJpaRepositories(basePackages = {
        "net.playercounts.models.repository"
})
public class PlayercountsPollWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayercountsPollWorkerApplication.class, args);
    }

}
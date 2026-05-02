package net.playercounts.statusconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "net.playercounts")
@EntityScan(basePackages = "net.playercounts.models")
@EnableJpaRepositories(basePackages = "net.playercounts.statusconsumer.repository")
@EnableScheduling
public class PlayercountsStatusConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayercountsStatusConsumerApplication.class, args);
    }

}
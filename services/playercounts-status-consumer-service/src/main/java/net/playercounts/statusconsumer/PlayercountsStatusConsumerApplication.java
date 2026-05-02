package net.playercounts.statusconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.playercounts")
@EntityScan(basePackages = "net.playercounts.models")
@EnableJpaRepositories(basePackages = "net.playercounts.statusconsumer.repository")
public class PlayercountsStatusConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayercountsStatusConsumerApplication.class, args);
    }

}
package net.playercounts.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "net.playercounts")
@EntityScan(basePackages = "net.playercounts.models")
@EnableScheduling
public class PlayercountsApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayercountsApiGatewayApplication.class, args);
    }

}
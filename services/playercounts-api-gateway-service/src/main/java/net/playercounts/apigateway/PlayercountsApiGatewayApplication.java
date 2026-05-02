package net.playercounts.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "net.playercounts")
@EntityScan(basePackages = "net.playercounts.models")
public class PlayercountsApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayercountsApiGatewayApplication.class, args);
    }

}
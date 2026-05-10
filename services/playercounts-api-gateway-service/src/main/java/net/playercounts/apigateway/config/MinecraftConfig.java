package net.playercounts.apigateway.config;

import net.playercounts.service.MinecraftPingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinecraftConfig {

    @Bean
    public MinecraftPingService minecraftPingService() {
        return new MinecraftPingService();
    }

}
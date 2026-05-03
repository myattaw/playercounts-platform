package net.playercounts.statusconsumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class ClickHouseConfig {

    @Bean
    public Connection clickHouseConnection(@Value("${clickhouse.jdbc-url}") String jdbcUrl,
                                           @Value("${clickhouse.username}") String username,
                                           @Value("${clickhouse.password}") String password) throws Exception {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
    
}
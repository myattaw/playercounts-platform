package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPingResultListener {

    private final StringRedisTemplate redisTemplate;

    public ServerPingResultListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "server-ping-results", groupId = "playercounts-status-consumer-group")
    public void consume(ServerPingResultEvent event) {
        System.out.println("STATUS CONSUMER RECEIVED EVENT -> " + event);

        redisTemplate.opsForValue().set(
                "live:server:" + event.getServerAddress(),
                String.valueOf(event.getOnlinePlayers())
        );

        System.out.println("REDIS UPDATED -> live:server:" + event.getServerAddress() + "=" + event.getOnlinePlayers());
    }
}
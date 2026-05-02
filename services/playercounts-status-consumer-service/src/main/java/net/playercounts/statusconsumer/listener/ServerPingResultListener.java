package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.models.ServerLatestStatus;
import net.playercounts.statusconsumer.repository.ServerLatestStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPingResultListener {

    private final StringRedisTemplate redisTemplate;
    private final ServerLatestStatusRepository statusRepository;

    @Value("${status-consumer.kafka-topic}")
    private String kafkaTopic;

    public ServerPingResultListener(StringRedisTemplate redisTemplate,
                                    ServerLatestStatusRepository statusRepository) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
    }

    @KafkaListener(
            topics = "${status-consumer.kafka-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ServerPingResultEvent event) {
        System.out.println("STATUS CONSUMER RECEIVED EVENT -> " + event);

        redisTemplate.opsForValue().set(
                "live:server:" + event.getServerAddress(),
                String.valueOf(event.getOnlinePlayers())
        );

        ServerLatestStatus latestStatus = new ServerLatestStatus(
                event.getServerAddress(),
                event.getOnlinePlayers(),
                event.getMaxPlayers(),
                event.getLatencyMs(),
                event.isOnline(),
                event.getTimestamp()
        );

        statusRepository.save(latestStatus);

        System.out.println("STATUS CONSUMER RECEIVED EVENT -> " + event.getServerAddress());


    }

}
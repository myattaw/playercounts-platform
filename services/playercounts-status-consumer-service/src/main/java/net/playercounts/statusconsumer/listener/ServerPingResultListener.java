package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.models.ServerLatestStatus;
import net.playercounts.statusconsumer.repository.ServerLatestStatusRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPingResultListener {

    private final StringRedisTemplate redisTemplate;
    private final ServerLatestStatusRepository statusRepository;

    public ServerPingResultListener(StringRedisTemplate redisTemplate,
                                    ServerLatestStatusRepository statusRepository) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
    }

    @KafkaListener(topics = "server-ping-results", groupId = "playercounts-status-consumer-group")
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
                event.getTimestamp()
        );

        statusRepository.save(latestStatus);

        System.out.println("POSTGRES UPSERT COMPLETE -> " + event.getServerAddress());
    }

}
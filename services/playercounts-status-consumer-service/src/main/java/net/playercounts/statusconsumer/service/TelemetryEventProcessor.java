package net.playercounts.statusconsumer.service;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.models.ServerLatestStatus;
import net.playercounts.statusconsumer.analytics.HistoricalTelemetryWriter;
import net.playercounts.statusconsumer.repository.ServerLatestStatusRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryEventProcessor {

    private final StringRedisTemplate redisTemplate;
    private final ServerLatestStatusRepository statusRepository;
    private final HistoricalTelemetryWriter historicalTelemetryWriter;

    private int processedCounter = 0;
    private long lastLogTime = System.currentTimeMillis();

    public TelemetryEventProcessor(StringRedisTemplate redisTemplate,
                                   ServerLatestStatusRepository statusRepository, HistoricalTelemetryWriter historicalTelemetryWriter) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
        this.historicalTelemetryWriter = historicalTelemetryWriter;
    }

    public void process(ServerPingResultEvent event) {

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

        processedCounter++;

        long now = System.currentTimeMillis();
        if (now - lastLogTime >= 5000) {
            System.out.println("STATUS CONSUMER BATCH COMPLETE -> materialized " + processedCounter + " events");
            processedCounter = 0;
            lastLogTime = now;
        }

        historicalTelemetryWriter.append(event);
    }
}
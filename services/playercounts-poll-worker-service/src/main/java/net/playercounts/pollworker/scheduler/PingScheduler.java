package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.contracts.publisher.TelemetryEventPublisher;
import net.playercounts.models.MinecraftPingResult;
import net.playercounts.models.entity.TrackedServer;
import net.playercounts.pollworker.service.TrackedServerService;
import net.playercounts.service.MinecraftPingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class PingScheduler {

    private final TelemetryEventPublisher telemetryEventPublisher;
    private final TrackedServerService trackedServerService;
    private final MinecraftPingService minecraftPingService;
    private final ExecutorService pollWorkerExecutor;

    private final StringRedisTemplate redisTemplate;

    @Value("${poll-worker.minecraft-default-port}")
    private int minecraftPort;

    public PingScheduler(
            TelemetryEventPublisher telemetryEventPublisher,
            TrackedServerService trackedServerService,
            MinecraftPingService minecraftPingService,
            ExecutorService pollWorkerExecutor,
            StringRedisTemplate redisTemplate
    ) {

        this.telemetryEventPublisher =
                telemetryEventPublisher;

        this.trackedServerService =
                trackedServerService;

        this.minecraftPingService =
                minecraftPingService;

        this.pollWorkerExecutor =
                pollWorkerExecutor;

        this.redisTemplate =
                redisTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void publishRealPingBatch() {

        long batchStart =
                System.currentTimeMillis();

        List<TrackedServer> servers =
                trackedServerService.getActiveServers();

        long now =
                System.currentTimeMillis();

        List<CompletableFuture<Void>> futures =
                servers.stream()
                        .filter(server -> {
                            long interval = calculatePollInterval(server);
                            return now - server.getUpdatedAt() >= interval;
                        })
                        .map(server ->
                                CompletableFuture.runAsync(() -> pollServer(server), pollWorkerExecutor)
                        )
                        .toList();

        CompletableFuture
                .allOf(
                        futures.toArray(
                                new CompletableFuture[0]
                        )
                )
                .join();

        long durationMs =
                System.currentTimeMillis()
                        - batchStart;

        System.out.println(

                "POLL WORKER BATCH COMPLETE -> emitted "

                        + futures.size()

                        + " adaptive minecraft ping events in "

                        + durationMs

                        + "ms"
        );
    }

    private long calculatePollInterval(
            TrackedServer server
    ) {

        boolean activelyWatched =
                redisTemplate.hasKey(MessageFormat.format("watching:{0}", server.getAddress()));

        // watched by active users
        if (activelyWatched) {
            return 10_000;
        }

        // large/high traffic servers
        if (server.getCurrentPlayers() >= 1000) {
            return 30_000;
        }

        // offline / inactive servers
        if (!server.isActive()) {
            return 300_000;
        }

        // default polling interval
        return 45_000;
    }

    private void pollServer(
            TrackedServer server
    ) {

        try {

            MinecraftPingResult pingResult =
                    minecraftPingService.ping(
                            server.getAddress(),
                            minecraftPort
                    );

            ServerPingResultEvent event =
                    new ServerPingResultEvent(
                            server.getAddress(),
                            pingResult.onlinePlayers(),
                            pingResult.maxPlayers(),
                            pingResult.latencyMs(),
                            pingResult.online(),
                            pingResult.icon() != null
                                    ? Base64.getEncoder().encodeToString(pingResult.icon())
                                    : null,
                            System.currentTimeMillis()
                    );

            telemetryEventPublisher.publish(
                    event
            );

        } catch (Exception e) {
            System.err.println("Failed to poll server: " + server.getAddress());
            e.printStackTrace();
        }
    }
}
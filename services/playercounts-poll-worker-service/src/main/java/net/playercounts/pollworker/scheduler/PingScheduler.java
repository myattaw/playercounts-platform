package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.contracts.publisher.TelemetryEventPublisher;
import net.playercounts.models.MinecraftPingResult;
import net.playercounts.models.entity.TrackedServer;
import net.playercounts.pollworker.service.TrackedServerService;
import net.playercounts.service.MinecraftPingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Value("${poll-worker.minecraft-default-port}")
    private int minecraftPort;

    public PingScheduler(
            TelemetryEventPublisher telemetryEventPublisher,
            TrackedServerService trackedServerService,
            MinecraftPingService minecraftPingService,
            ExecutorService pollWorkerExecutor
    ) {
        this.telemetryEventPublisher = telemetryEventPublisher;
        this.trackedServerService = trackedServerService;
        this.minecraftPingService = minecraftPingService;
        this.pollWorkerExecutor = pollWorkerExecutor;
    }

    @Scheduled(fixedRateString = "${poll-worker.scheduler-interval-ms}")
    public void publishRealPingBatch() {

        long batchStart = System.currentTimeMillis();

        List<TrackedServer> servers = trackedServerService.getActiveServers();

        List<CompletableFuture<Void>> futures = servers.stream()
                .map(server -> CompletableFuture.runAsync(() -> {

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

                                    // only include icon if present
                                    pingResult.icon() != null
                                            ? Base64.getEncoder()
                                            .encodeToString(pingResult.icon())
                                            : null,

                                    System.currentTimeMillis()
                            );

                    telemetryEventPublisher.publish(event);

                }, pollWorkerExecutor))
                .toList();

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        long durationMs = System.currentTimeMillis() - batchStart;

        System.out.println(
                "POLL WORKER BATCH COMPLETE2 -> emitted "
                        + servers.size()
                        + " minecraft ping events in "
                        + durationMs
                        + "ms"
        );
    }

}
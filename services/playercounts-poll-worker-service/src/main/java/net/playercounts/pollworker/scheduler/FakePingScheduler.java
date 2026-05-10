package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.contracts.publisher.TelemetryEventPublisher;
import net.playercounts.models.MinecraftPingResult;
import net.playercounts.pollworker.service.FakeServerRegistryService;
import net.playercounts.service.MinecraftPingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class FakePingScheduler {

    private final TelemetryEventPublisher telemetryEventPublisher;
    private final FakeServerRegistryService registryService;
    private final MinecraftPingService minecraftPingService;
    private final ExecutorService pollWorkerExecutor;

    @Value("${poll-worker.minecraft-default-port}")
    private int minecraftPort;

    @Value("${poll-worker.kafka-topic}")
    private String kafkaTopic;

    public FakePingScheduler(TelemetryEventPublisher telemetryEventPublisher,
                             FakeServerRegistryService registryService,
                             MinecraftPingService minecraftPingService,
                             ExecutorService pollWorkerExecutor) {
        this.telemetryEventPublisher = telemetryEventPublisher;
        this.registryService = registryService;
        this.minecraftPingService = minecraftPingService;
        this.pollWorkerExecutor = pollWorkerExecutor;
    }

    @Scheduled(fixedRateString = "${poll-worker.scheduler-interval-ms}")
    public void publishRealPingBatch() {
        long batchStart = System.currentTimeMillis();

        List<String> servers = registryService.getTrackedServers();

        List<CompletableFuture<Void>> futures = servers.stream()
                .map(serverAddress -> CompletableFuture.runAsync(() -> {
                    MinecraftPingResult pingResult = minecraftPingService.ping(serverAddress, minecraftPort);

                    ServerPingResultEvent event = new ServerPingResultEvent(
                            serverAddress,
                            pingResult.onlinePlayers(),
                            pingResult.maxPlayers(),
                            pingResult.latencyMs(),
                            pingResult.online(),
                            pingResult.icon() != null ? java.util.Base64.getEncoder().encodeToString(pingResult.icon()) : null,
                            System.currentTimeMillis()
                    );

                    telemetryEventPublisher.publish(event);

                }, pollWorkerExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long durationMs = System.currentTimeMillis() - batchStart;

        System.out.println("POLL WORKER BATCH COMPLETE -> emitted "
                + servers.size()
                + " real minecraft ping events in "
                + durationMs
                + "ms");
    }

}
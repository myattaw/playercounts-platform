package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.pollworker.model.MinecraftPingResult;
import net.playercounts.pollworker.service.FakeServerRegistryService;
import net.playercounts.pollworker.service.MinecraftPingService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FakePingScheduler {

    private final KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate;
    private final FakeServerRegistryService registryService;
    private final MinecraftPingService minecraftPingService;

    public FakePingScheduler(KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate,
                             FakeServerRegistryService registryService,
                             MinecraftPingService minecraftPingService) {
        this.kafkaTemplate = kafkaTemplate;
        this.registryService = registryService;
        this.minecraftPingService = minecraftPingService;
    }

    @Scheduled(fixedRate = 15000)
    public void publishRealPingBatch() {

        int emitted = 0;

        for (String serverAddress : registryService.getTrackedServers()) {

            MinecraftPingResult pingResult = minecraftPingService.ping(serverAddress, 25565);

            ServerPingResultEvent event = new ServerPingResultEvent(
                    serverAddress,
                    pingResult.getOnlinePlayers(),
                    pingResult.getMaxPlayers(),
                    pingResult.getLatencyMs(),
                    pingResult.isOnline(),
                    System.currentTimeMillis()
            );

            kafkaTemplate.send("server-ping-results", event);
            emitted++;
        }

        System.out.println("POLL WORKER BATCH COMPLETE -> emitted " + emitted + " REAL minecraft ping events");
    }

}
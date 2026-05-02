package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.pollworker.service.FakeServerRegistryService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class FakePingScheduler {

    private final KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate;
    private final FakeServerRegistryService registryService;
    private final Random random = new Random();

    public FakePingScheduler(KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate,
                             FakeServerRegistryService registryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.registryService = registryService;
    }

    @Scheduled(fixedRate = 5000)
    public void publishFakePingBatch() {

        for (String serverAddress : registryService.getTrackedServers()) {

            int players = 100 + random.nextInt(50000);
            int maxPlayers = 1000 + random.nextInt(200000);

            ServerPingResultEvent event = new ServerPingResultEvent(
                    serverAddress,
                    players,
                    maxPlayers,
                    System.currentTimeMillis()
            );

            kafkaTemplate.send("server-ping-results", event);
        }

        System.out.println("POLL WORKER BATCH COMPLETE -> emitted "
                + registryService.getTrackedServers().size() + " server ping events");
    }
}
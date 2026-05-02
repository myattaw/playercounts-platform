package net.playercounts.pollworker.scheduler;

import net.playercounts.contracts.ServerPingResultEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class FakePingScheduler {

    private final KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate;
    private final Random random = new Random();

    public FakePingScheduler(KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void publishFakePing() {
        int players = 40000 + random.nextInt(5000);

        ServerPingResultEvent event = new ServerPingResultEvent(
                "hypixel.net",
                players,
                200000,
                System.currentTimeMillis()
        );

        kafkaTemplate.send("server-ping-results", event);

        System.out.println("POLL WORKER SENT EVENT -> " + event);
    }

}
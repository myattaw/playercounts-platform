package net.playercounts.pollworker.runner;

import net.playercounts.contracts.ServerPingResultEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FakePingPublisherRunner implements CommandLineRunner {

    private final KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate;

    public FakePingPublisherRunner(KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        ServerPingResultEvent event = new ServerPingResultEvent(
                "hypixel.net",
                42137,
                200000,
                System.currentTimeMillis()
        );

        kafkaTemplate.send("server-ping-results", event);

        System.out.println("POLL WORKER SENT EVENT -> " + event);
    }
}
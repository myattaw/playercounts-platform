package net.playercounts.pollworker.messaging;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.contracts.publisher.TelemetryEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTelemetryEventPublisher implements TelemetryEventPublisher {

    private final KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate;

    @Value("${poll-worker.kafka-topic}")
    private String kafkaTopic;

    public KafkaTelemetryEventPublisher(KafkaTemplate<String, ServerPingResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(ServerPingResultEvent event) {
        kafkaTemplate.send(kafkaTopic, event.getServerAddress(), event);
    }
}
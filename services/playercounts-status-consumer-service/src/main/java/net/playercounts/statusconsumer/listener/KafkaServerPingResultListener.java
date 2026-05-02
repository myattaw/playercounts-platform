package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.statusconsumer.service.TelemetryEventProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("local") // Use Kafka when running locally
public class KafkaServerPingResultListener {

    private final TelemetryEventProcessor telemetryEventProcessor;

    public KafkaServerPingResultListener(TelemetryEventProcessor telemetryEventProcessor) {
        this.telemetryEventProcessor = telemetryEventProcessor;
    }

    @KafkaListener(topics = "${status-consumer.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ServerPingResultEvent event) {
        telemetryEventProcessor.process(event);
    }

}
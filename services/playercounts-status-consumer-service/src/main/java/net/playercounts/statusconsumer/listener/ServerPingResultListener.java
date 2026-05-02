package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.statusconsumer.service.TelemetryEventProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPingResultListener {

    private final TelemetryEventProcessor telemetryEventProcessor;

    public ServerPingResultListener(TelemetryEventProcessor telemetryEventProcessor) {
        this.telemetryEventProcessor = telemetryEventProcessor;
    }

    @KafkaListener(topics = "${status-consumer.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ServerPingResultEvent event) {
        telemetryEventProcessor.process(event);
    }

}
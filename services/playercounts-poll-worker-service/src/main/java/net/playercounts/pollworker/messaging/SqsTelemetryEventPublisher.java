package net.playercounts.pollworker.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.contracts.publisher.TelemetryEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@Profile("cloud")
public class SqsTelemetryEventPublisher implements TelemetryEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloud.sqs.queue-url}")
    private String queueUrl;

    public SqsTelemetryEventPublisher(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public void publish(ServerPingResultEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish telemetry event to SQS", e);
        }
    }

}
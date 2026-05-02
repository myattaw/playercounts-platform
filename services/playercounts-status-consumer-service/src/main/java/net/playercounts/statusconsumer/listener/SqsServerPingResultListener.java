package net.playercounts.statusconsumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.playercounts.contracts.ServerPingResultEvent;
import net.playercounts.statusconsumer.service.TelemetryEventProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

@Component
@Profile("cloud")
public class SqsServerPingResultListener {

    private final SqsClient sqsClient;
    private final TelemetryEventProcessor telemetryEventProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloud.sqs.queue-url}")
    private String queueUrl;

    public SqsServerPingResultListener(SqsClient sqsClient,
                                       TelemetryEventProcessor telemetryEventProcessor) {
        this.sqsClient = sqsClient;
        this.telemetryEventProcessor = telemetryEventProcessor;
    }

    @Scheduled(fixedDelay = 3000)
    public void pollQueue() {
        List<Message> messages = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(2)
                        .build()
        ).messages();

        for (Message message : messages) {
            try {
                ServerPingResultEvent event = objectMapper.readValue(message.body(), ServerPingResultEvent.class);
                telemetryEventProcessor.process(event);

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());

            } catch (Exception e) {
                System.out.println("SQS MESSAGE PROCESS FAILURE -> " + e.getMessage());
            }
        }
    }

}
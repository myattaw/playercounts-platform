package net.playercounts.statusconsumer.listener;

import net.playercounts.contracts.ServerPingResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPingResultListener {

    @KafkaListener(topics = "server-ping-results", groupId = "playercounts-status-consumer-group")
    public void consume(ServerPingResultEvent event) {
        System.out.println("STATUS CONSUMER RECEIVED EVENT -> " + event);
    }
}
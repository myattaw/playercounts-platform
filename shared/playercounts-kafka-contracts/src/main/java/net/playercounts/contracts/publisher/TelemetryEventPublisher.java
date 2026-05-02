package net.playercounts.contracts.publisher;

import net.playercounts.contracts.ServerPingResultEvent;

public interface TelemetryEventPublisher {

    void publish(ServerPingResultEvent event);

}
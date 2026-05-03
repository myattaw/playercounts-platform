package net.playercounts.statusconsumer.analytics;

import net.playercounts.contracts.ServerPingResultEvent;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Component
public class HistoricalTelemetryWriter {

    private final Connection clickHouseConnection;

    public HistoricalTelemetryWriter(Connection clickHouseConnection) {
        this.clickHouseConnection = clickHouseConnection;
    }

    public void append(ServerPingResultEvent event) {
        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    INSERT INTO server_ping_history
                    (server_address, online_players, max_players, latency_ms, online, event_timestamp)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """);

            statement.setString(1, event.getServerAddress());
            statement.setInt(2, event.getOnlinePlayers());
            statement.setInt(3, event.getMaxPlayers());
            statement.setLong(4, event.getLatencyMs());
            statement.setBoolean(5, event.isOnline());
            statement.setTimestamp(6, new Timestamp(event.getTimestamp()));

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
package net.playercounts.apigateway.repository;

import net.playercounts.models.HistoricalPingPoint;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistoricalTelemetryRepository {

    private final Connection clickHouseConnection;

    public HistoricalTelemetryRepository(Connection clickHouseConnection) {
        this.clickHouseConnection = clickHouseConnection;
    }

    public List<HistoricalPingPoint> getHistory(String address) {
        List<HistoricalPingPoint> results = new ArrayList<>();

        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    SELECT server_address, online_players, max_players, latency_ms, online, event_timestamp
                    FROM server_ping_history
                    WHERE server_address = ?
                    ORDER BY event_timestamp DESC
                    LIMIT 500
                    """);

            statement.setString(1, address);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                HistoricalPingPoint point = new HistoricalPingPoint(
                        rs.getString("server_address"),
                        rs.getInt("online_players"),
                        rs.getInt("max_players"),
                        rs.getInt("latency_ms"),
                        rs.getBoolean("online"),
                        rs.getTimestamp("event_timestamp").getTime()
                );
                results.add(point);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
package net.playercounts.apigateway.repository;

import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.snapshot.TopServerSnapshot;
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

    public ServerAnalyticsSnapshot getAnalytics(String address, int currentPlayers) {
        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    SELECT
                        max(online_players) as peak_players,
                        avg(online_players) as avg_players,
                        avg(latency_ms) as avg_latency,
                        avg(if(online = 1, 100, 0)) as uptime_percent,
                        min(event_timestamp) as first_seen,
                        max(event_timestamp) as last_seen
                    FROM server_ping_history
                    WHERE server_address = ?
                    """);

            statement.setString(1, address);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new ServerAnalyticsSnapshot(
                        address,
                        currentPlayers,
                        rs.getInt("peak_players"),
                        (int) Math.round(rs.getDouble("avg_players")),
                        (int) Math.round(rs.getDouble("avg_latency")),
                        rs.getDouble("uptime_percent"),
                        rs.getTimestamp("first_seen").getTime(),
                        rs.getTimestamp("last_seen").getTime()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TopServerSnapshot> getTopPeakServers() {
        List<TopServerSnapshot> results = new ArrayList<>();

        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    SELECT
                        server_address,
                        anyLast(online_players) as current_players,
                        max(online_players) as peak_players,
                        avg(online_players) as avg_players
                    FROM server_ping_history
                    GROUP BY server_address
                    ORDER BY peak_players DESC
                    LIMIT 10
                    """);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                results.add(new TopServerSnapshot(
                        rs.getString("server_address"),
                        0,
                        rs.getInt("peak_players"),
                        (int) Math.round(rs.getDouble("avg_players"))
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<TopServerSnapshot> getTopTrendingServers() {
        List<TopServerSnapshot> results = new ArrayList<>();

        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    SELECT
                        server_address,
                        anyLast(online_players) as current_players,
                        max(online_players) as peak_players,
                        avg(online_players) as avg_players
                    FROM server_ping_history
                    GROUP BY server_address
                    ORDER BY avg_players DESC
                    LIMIT 10
                    """);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                results.add(new TopServerSnapshot(
                        rs.getString("server_address"),
                        rs.getInt("current_players"),
                        rs.getInt("peak_players"),
                        (int) Math.round(rs.getDouble("avg_players"))
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<TopServerSnapshot> getTopLiveServers() {
        List<TopServerSnapshot> results = new ArrayList<>();

        try {

            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                    SELECT
                        server_address,
                        anyLast(online_players) as current_players,
                        max(online_players) as peak_players,
                        avg(online_players) as avg_players
                    FROM server_ping_history
                    GROUP BY server_address
                    ORDER BY current_players DESC
                    LIMIT 10
                    """);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                results.add(new TopServerSnapshot(
                        rs.getString("server_address"),
                        rs.getInt("current_players"),
                        rs.getInt("peak_players"),
                        (int) Math.round(rs.getDouble("avg_players"))
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

}
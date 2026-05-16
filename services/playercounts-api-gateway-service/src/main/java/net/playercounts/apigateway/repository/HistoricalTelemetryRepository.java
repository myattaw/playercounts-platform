package net.playercounts.apigateway.repository;

import net.playercounts.apigateway.repository.row.ServerAggregateRow;
import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.snapshot.graph.GraphHistoryPoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class HistoricalTelemetryRepository {

    private final Connection clickHouseConnection;
    private final StringRedisTemplate redisTemplate;

    public HistoricalTelemetryRepository(Connection clickHouseConnection,
                                         StringRedisTemplate redisTemplate) {
        this.clickHouseConnection = clickHouseConnection;
        this.redisTemplate = redisTemplate;
    }

    public List<ServerAggregateRow> getServerAggregates() {

        List<ServerAggregateRow> rows = new ArrayList<>();

        try (PreparedStatement statement =
                     clickHouseConnection.prepareStatement("""

    SELECT
        server_address,

        argMax(online_players, event_timestamp)
            AS current_players,

        max(online_players)
            AS peak_players,

        avg(online_players)
            AS avg_players,

        -- Last 24h average
        avgIf(
            online_players,
            event_timestamp >= now() - INTERVAL 24 HOUR
        ) AS recent_24h_avg,

        -- Previous 24h average
        avgIf(
            online_players,
            event_timestamp BETWEEN
                now() - INTERVAL 48 HOUR
                AND
                now() - INTERVAL 24 HOUR
        ) AS previous_24h_avg,

        -- Last 7d average
        avgIf(
            online_players,
            event_timestamp >= now() - INTERVAL 7 DAY
        ) AS recent_7d_avg,

        -- Previous 7d average
        avgIf(
            online_players,
            event_timestamp BETWEEN
                now() - INTERVAL 14 DAY
                AND
                now() - INTERVAL 7 DAY
        ) AS previous_7d_avg,

        -- Last 30d average
        avgIf(
            online_players,
            event_timestamp >= now() - INTERVAL 30 DAY
        ) AS recent_30d_avg,

        -- Previous 30d average
        avgIf(
            online_players,
            event_timestamp BETWEEN
                now() - INTERVAL 60 DAY
                AND
                now() - INTERVAL 30 DAY
        ) AS previous_30d_avg

    FROM server_ping_history

    GROUP BY server_address

    """);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                int avgPlayers =
                        (int) Math.round(
                                rs.getDouble("avg_players")
                        );

                double growth24h =
                        calculateGrowth(
                                rs.getDouble("recent_24h_avg"),
                                rs.getDouble("previous_24h_avg")
                        );

                double growth7d =
                        calculateGrowth(
                                rs.getDouble("recent_7d_avg"),
                                rs.getDouble("previous_7d_avg")
                        );

                double growth30d =
                        calculateGrowth(
                                rs.getDouble("recent_30d_avg"),
                                rs.getDouble("previous_30d_avg")
                        );

                double trendingScore = 0;

                if (avgPlayers >= 100) {

                    trendingScore =
                            growth24h
                                    * Math.log10(
                                    Math.max(avgPlayers, 1)
                            );
                }

                rows.add(new ServerAggregateRow(

                        rs.getString("server_address"),

                        rs.getInt("current_players"),

                        rs.getInt("peak_players"),

                        avgPlayers,

                        growth24h,

                        growth7d,

                        growth30d,

                        trendingScore
                ));
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load server aggregates",
                    e
            );
        }

        return rows;
    }

    public long getTotalTelemetryPoints() {
        try (PreparedStatement statement = clickHouseConnection.prepareStatement("""
            SELECT count() AS total_points
            FROM server_ping_history
        """);
             ResultSet rs = statement.executeQuery()) {

            return rs.next() ? rs.getLong("total_points") : 0L;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load telemetry point count", e);
        }
    }

    public Map<String, List<GraphHistoryPoint>> getGraphHistories(Collection<String> addresses) {
        Map<String, List<GraphHistoryPoint>> histories = new HashMap<>();

        if (addresses == null || addresses.isEmpty()) {
            return histories;
        }

        List<String> addressList = new ArrayList<>(addresses);
        String placeholders = String.join(",", Collections.nCopies(addressList.size(), "?"));

        String sql = """
            SELECT
                server_address,
                toUnixTimestamp(toStartOfInterval(event_timestamp, INTERVAL 10 MINUTE)) * 1000 AS bucket_ts,
                avg(online_players) AS avg_players
            FROM server_ping_history
            WHERE event_timestamp >= now() - INTERVAL 24 HOUR
              AND server_address IN (%s)
            GROUP BY server_address, bucket_ts
            ORDER BY server_address, bucket_ts ASC
        """.formatted(placeholders);

        try (PreparedStatement statement = clickHouseConnection.prepareStatement(sql)) {
            for (int i = 0; i < addressList.size(); i++) {
                statement.setString(i + 1, addressList.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String address = rs.getString("server_address");

                    histories
                            .computeIfAbsent(address, ignored -> new ArrayList<>())
                            .add(new GraphHistoryPoint(
                                    rs.getLong("bucket_ts"),
                                    (int) Math.round(rs.getDouble("avg_players"))
                            ));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load graph histories", e);
        }

        return histories;
    }

    public String getServerIcon(String address) {
        return redisTemplate.opsForValue().get("icon:server:" + address);
    }

    public Map<String, String> getServerIcons(Collection<String> addresses) {
        Map<String, String> icons = new HashMap<>();

        for (String address : addresses) {
            icons.put(address, getServerIcon(address));
        }

        return icons;
    }

    public List<HistoricalPingPoint> getHistory(String address) {
        List<HistoricalPingPoint> results = new ArrayList<>();

        try (PreparedStatement statement = clickHouseConnection.prepareStatement("""
            SELECT
                server_address,
                online_players,
                max_players,
                latency_ms,
                online,
                event_timestamp
            FROM server_ping_history
            WHERE server_address = ?
            ORDER BY event_timestamp DESC
            LIMIT 500
            """)) {

            statement.setString(1, address);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new HistoricalPingPoint(
                            rs.getString("server_address"),
                            rs.getInt("online_players"),
                            rs.getInt("max_players"),
                            rs.getInt("latency_ms"),
                            rs.getBoolean("online"),
                            rs.getTimestamp("event_timestamp").getTime()
                    ));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load history for " + address, e);
        }

        return results;
    }

    public ServerAnalyticsSnapshot getAnalytics(String address, int currentPlayers) {
        try (PreparedStatement statement = clickHouseConnection.prepareStatement("""
            SELECT
                max(online_players) AS peak_players,
                avg(online_players) AS avg_players,
                avg(latency_ms) AS avg_latency,
                avg(if(online = 1, 100, 0)) AS uptime_percent,
                min(event_timestamp) AS first_seen,
                max(event_timestamp) AS last_seen
            FROM server_ping_history
            WHERE server_address = ?
            """)) {

            statement.setString(1, address);

            try (ResultSet rs = statement.executeQuery()) {
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
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load analytics for " + address, e);
        }

        return null;
    }

    private double calculateGrowth(
            double current,
            double previous
    ) {

        if (
                Double.isNaN(current)
                        || Double.isNaN(previous)
                        || Double.isInfinite(current)
                        || Double.isInfinite(previous)
                        || previous <= 0
        ) {
            return 0;
        }

        double growth =
                ((current - previous) / previous)
                        * 100.0;

        if (
                Double.isNaN(growth)
                        || Double.isInfinite(growth)
        ) {
            return 0;
        }

        return Math.round(growth * 100.0) / 100.0;
    }

}
package net.playercounts.apigateway.repository;

import net.playercounts.apigateway.repository.row.ServerAggregateRow;
import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.snapshot.graph.GraphHistoryPoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                             
                                     argMax(
                                         online_players,
                                         event_timestamp
                                     ) AS current_players,
                             
                                     max(online_players)
                                         AS peak_players,
                             
                                     avg(online_players)
                                         AS avg_players
                             
                                 FROM server_ping_history
                             
                                 WHERE event_timestamp >= now() - INTERVAL 30 DAY
                             
                                 GROUP BY server_address
                             """);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                rows.add(new ServerAggregateRow(

                        rs.getString("server_address"),

                        rs.getInt("current_players"),

                        rs.getInt("peak_players"),

                        (int) Math.round(
                                rs.getDouble("avg_players")
                        ),

                        0,
                        0,
                        0,
                        0
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

    public List<ServerAggregateRow> getTrendingServers() {

        List<ServerAggregateRow> rows = new ArrayList<>();

        try (PreparedStatement statement =
                     clickHouseConnection.prepareStatement("""
                             
                                 SELECT
                                     server_address,
                             
                                     argMax(
                                         online_players,
                                         event_timestamp
                                     ) AS current_players,
                             
                                     max(online_players)
                                         AS peak_players,
                             
                                     avg(online_players)
                                         AS avg_players,
                             
                                     -- Last 15 minutes
                                     avgIf(
                                         online_players,
                                         event_timestamp >= now() - INTERVAL 15 MINUTE
                                     ) AS current_15m_avg,
                             
                                     -- 15-30 minutes ago
                                     avgIf(
                                         online_players,
                                         event_timestamp BETWEEN
                                             now() - INTERVAL 30 MINUTE
                                             AND now() - INTERVAL 15 MINUTE
                                     ) AS previous_15m_avg,
                             
                                     -- Last 1 hour
                                     avgIf(
                                         online_players,
                                         event_timestamp >= now() - INTERVAL 1 HOUR
                                     ) AS current_1h_avg,
                             
                                     -- 1-2 hours ago
                                     avgIf(
                                         online_players,
                                         event_timestamp BETWEEN
                                             now() - INTERVAL 2 HOUR
                                             AND now() - INTERVAL 1 HOUR
                                     ) AS previous_1h_avg,
                             
                                     -- Last 6 hours
                                     avgIf(
                                         online_players,
                                         event_timestamp >= now() - INTERVAL 6 HOUR
                                     ) AS current_6h_avg,
                             
                                     -- 6-12 hours ago
                                     avgIf(
                                         online_players,
                                         event_timestamp BETWEEN
                                             now() - INTERVAL 12 HOUR
                                             AND now() - INTERVAL 6 HOUR
                                     ) AS previous_6h_avg
                             
                                 FROM server_ping_history
                             
                                 WHERE event_timestamp >= now() - INTERVAL 24 HOUR
                             
                                 GROUP BY server_address
                             
                                 HAVING
                                     count() >= 3
                                     AND current_players >= 100
                             
                             """);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                String serverAddress =
                        rs.getString("server_address");

                int currentPlayers =
                        rs.getInt("current_players");

                int peakPlayers =
                        rs.getInt("peak_players");

                int avgPlayers =
                        (int) Math.round(
                                rs.getDouble("avg_players")
                        );

                double growth15m =
                        calculateGrowth(
                                rs.getDouble("current_15m_avg"),
                                rs.getDouble("previous_15m_avg")
                        );

                double growth1h =
                        calculateGrowth(
                                rs.getDouble("current_1h_avg"),
                                rs.getDouble("previous_1h_avg")
                        );

                double growth6h =
                        calculateGrowth(
                                rs.getDouble("current_6h_avg"),
                                rs.getDouble("previous_6h_avg")
                        );

                // Stable weighted momentum
                double momentum =
                        (growth15m * 0.50)
                                + (growth1h * 0.35)
                                + (growth6h * 0.15);

                double trendingScore = momentum;

                // Prevent giant servers from dominating
                if (currentPlayers > 20000) {
                    trendingScore *= 0.10;
                } else if (currentPlayers > 10000) {
                    trendingScore *= 0.25;
                } else if (currentPlayers > 5000) {
                    trendingScore *= 0.50;
                }

                // Boost medium discovery servers
                if (currentPlayers < 3000) {
                    trendingScore *= 1.35;
                }

                // Strong spike boost
                if (growth15m >= 25) {

                    trendingScore *= 2.5;

                } else if (growth15m >= 10) {

                    trendingScore *= 1.5;
                }

                // Kill stagnant servers
                if (growth15m < 1
                        && growth1h < 2) {

                    trendingScore *= 0.15;
                }

                // Prevent negative scores
                trendingScore =
                        Math.max(trendingScore, 0);

                rows.add(new ServerAggregateRow(

                        serverAddress,

                        currentPlayers,

                        peakPlayers,

                        avgPlayers,

                        growth1h,

                        growth15m,

                        growth6h,

                        trendingScore
                ));
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load trending servers",
                    e
            );
        }

        rows.sort(
                Comparator.comparingDouble(
                        ServerAggregateRow::trendingScore
                ).reversed()
        );

        return rows.stream()
                .limit(10)
                .toList();
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
        ) {
            return 0;
        }

        // NEW SERVER / SPIKE DETECTION
        if (previous <= 0) {

            // If server suddenly became active,
            // treat as explosive momentum
            if (current >= 15) {
                return 100;
            }

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
package net.playercounts.apigateway.repository;

import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.platform.GraphHistoryPoint;
import net.playercounts.models.snapshot.platform.GraphServerSnapshot;
import net.playercounts.models.snapshot.platform.PlatformOverviewSnapshot;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.snapshot.TopServerSnapshot;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistoricalTelemetryRepository {

    private final Connection clickHouseConnection;
    private final StringRedisTemplate redisTemplate;

    public HistoricalTelemetryRepository(Connection clickHouseConnection,
                                         StringRedisTemplate redisTemplate) {
        this.clickHouseConnection = clickHouseConnection;
        this.redisTemplate = redisTemplate;
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

    public PlatformOverviewSnapshot getPlatformOverview() {
        try {
            PreparedStatement statement = clickHouseConnection.prepareStatement("""
                SELECT
                    countDistinct(server_address) as tracked_servers,
                    sum(current_players) as total_current_players,
                    avg(current_players) as avg_current_players,
                    argMax(server_address, current_players) as largest_server,
                    max(current_players) as largest_server_players
                FROM
                (
                    SELECT
                        server_address,
                        argMax(online_players, event_timestamp) as current_players
                    FROM server_ping_history
                    GROUP BY server_address
                )
                """);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int trackedServers = rs.getInt("tracked_servers");
                int totalCurrentPlayers = rs.getInt("total_current_players");
                double avgCurrentPlayers = rs.getDouble("avg_current_players");
                String largestServer = rs.getString("largest_server");
                int largestServerPlayers = rs.getInt("largest_server_players");

                PreparedStatement countStatement = clickHouseConnection.prepareStatement("""
                    SELECT count() as total_points
                    FROM server_ping_history
                    """);

                ResultSet countRs = countStatement.executeQuery();
                long totalPoints = 0;

                if (countRs.next()) {
                    totalPoints = countRs.getLong("total_points");
                }

                return new PlatformOverviewSnapshot(
                        trackedServers,
                        totalCurrentPlayers,
                        (int) Math.round(avgCurrentPlayers),
                        totalPoints,
                        largestServer,
                        largestServerPlayers
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<GraphServerSnapshot> getDashboardGraphServers() {
        List<GraphServerSnapshot> servers = new ArrayList<>();

        String[] colors = {
                "#f59e0b",
                "#10b981",
                "#3b82f6",
                "#8b5cf6",
                "#ec4899",
                "#14b8a6",
                "#f97316",
                "#06b6d4",
                "#a855f7",
                "#84cc16"
        };

        try {
            PreparedStatement topStatement = clickHouseConnection.prepareStatement("""
            SELECT
                server_address,
                argMax(online_players, event_timestamp) as current_players,
                max(online_players) as peak_players
            FROM server_ping_history
            GROUP BY server_address
            ORDER BY peak_players DESC
            LIMIT 8
            """);

            ResultSet topRs = topStatement.executeQuery();

            int rank = 1;

            while (topRs.next()) {
                String address = topRs.getString("server_address");
                int currentPlayers = topRs.getInt("current_players");
                int peakPlayers = topRs.getInt("peak_players");

                PreparedStatement historyStatement = clickHouseConnection.prepareStatement("""
                SELECT
                    toUnixTimestamp(toStartOfInterval(event_timestamp, INTERVAL 10 MINUTE)) * 1000 as bucket_ts,
                    avg(online_players) as avg_players
                FROM server_ping_history
                WHERE server_address = ?
                  AND event_timestamp >= now() - INTERVAL 24 HOUR
                GROUP BY bucket_ts
                ORDER BY bucket_ts ASC
                """);

                historyStatement.setString(1, address);

                ResultSet historyRs = historyStatement.executeQuery();

                List<GraphHistoryPoint> history = new ArrayList<>();

                while (historyRs.next()) {
                    history.add(new GraphHistoryPoint(
                            historyRs.getLong("bucket_ts"),
                            (int) Math.round(historyRs.getDouble("avg_players"))
                    ));
                }

                String iconBase64 = redisTemplate.opsForValue().get("icon:server:" + address);

                servers.add(new GraphServerSnapshot(
                        address,
                        currentPlayers,
                        peakPlayers,
                        history,
                        colors[(rank - 1) % colors.length],
                        rank,
                        iconBase64
                ));

                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return servers;
    }

}
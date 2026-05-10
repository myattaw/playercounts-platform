package net.playercounts.models.snapshot;

public record ServerAnalyticsSnapshot(
        String serverAddress,
        int currentPlayers,
        int peakPlayers24h,
        int averagePlayers24h,
        int averageLatencyMs,
        double uptimePercent24h,
        long firstSeenTimestamp,
        long lastSeenTimestamp
) {}
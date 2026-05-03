package net.playercounts.models;

public record HistoricalPingPoint(
        String serverAddress,
        int onlinePlayers,
        int maxPlayers,
        int latencyMs,
        boolean online,
        long timestamp
) {}
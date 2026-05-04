package net.playercounts.contracts;

public record ServerPingResultEvent(
        String serverAddress,
        int onlinePlayers,
        int maxPlayers,
        long latencyMs,
        boolean online,
        String iconBase64,
        long timestamp
) {
}
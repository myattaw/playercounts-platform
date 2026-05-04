package net.playercounts.pollworker.model;

public record MinecraftPingResult(
        boolean online,
        int onlinePlayers,
        int maxPlayers,
        long latencyMs,
        byte[] icon
) {
}
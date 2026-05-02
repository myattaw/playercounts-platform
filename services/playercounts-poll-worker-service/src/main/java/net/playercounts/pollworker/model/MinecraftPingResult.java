package net.playercounts.pollworker.model;

public class MinecraftPingResult {

    private final boolean online;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final long latencyMs;

    public MinecraftPingResult(boolean online, int onlinePlayers, int maxPlayers, long latencyMs) {
        this.online = online;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.latencyMs = latencyMs;
    }

    public boolean isOnline() {
        return online;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

}
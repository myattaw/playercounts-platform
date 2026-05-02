package net.playercounts.contracts;

public class ServerPingResultEvent {

    private String serverAddress;
    private int onlinePlayers;
    private int maxPlayers;
    private long latencyMs;
    private boolean online;
    private long timestamp;

    public ServerPingResultEvent() {
    }

    public ServerPingResultEvent(String serverAddress,
                                 int onlinePlayers,
                                 int maxPlayers,
                                 long latencyMs,
                                 boolean online,
                                 long timestamp) {
        this.serverAddress = serverAddress;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.latencyMs = latencyMs;
        this.online = online;
        this.timestamp = timestamp;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ServerPingResultEvent{" +
                "serverAddress='" + serverAddress + '\'' +
                ", onlinePlayers=" + onlinePlayers +
                ", maxPlayers=" + maxPlayers +
                ", latencyMs=" + latencyMs +
                ", online=" + online +
                ", timestamp=" + timestamp +
                '}';
    }
}
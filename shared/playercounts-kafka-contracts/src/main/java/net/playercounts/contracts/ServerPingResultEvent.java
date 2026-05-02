package net.playercounts.contracts;

public class ServerPingResultEvent {

    private String serverAddress;
    private int onlinePlayers;
    private int maxPlayers;
    private long timestamp;

    public ServerPingResultEvent() {
    }

    public ServerPingResultEvent(String serverAddress, int onlinePlayers, int maxPlayers, long timestamp) {
        this.serverAddress = serverAddress;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
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
                ", timestamp=" + timestamp +
                '}';
    }
}
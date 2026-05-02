package net.playercounts.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "server_latest_status")
public class ServerLatestStatus {

    @Id
    private String serverAddress;

    private int onlinePlayers;
    private int maxPlayers;
    private long timestamp;

    public ServerLatestStatus() {
    }

    public ServerLatestStatus(String serverAddress, int onlinePlayers, int maxPlayers, long timestamp) {
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
}
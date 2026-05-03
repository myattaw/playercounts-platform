package net.playercounts.models.snapshot;

public record TopServerSnapshot(
        String serverAddress,
        int currentPlayers,
        int peakPlayers,
        int averagePlayers
) {}
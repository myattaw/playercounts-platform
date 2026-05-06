package net.playercounts.models.snapshot.server;

public record SelectableServerSnapshot(
        String address,
        int currentPlayers,
        int peakPlayers,
        int rank,
        String iconBase64
) {
}
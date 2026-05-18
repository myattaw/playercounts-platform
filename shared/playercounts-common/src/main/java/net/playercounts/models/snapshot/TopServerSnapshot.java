package net.playercounts.models.snapshot;

public record TopServerSnapshot(
        String address,
        int currentPlayers,
        int peakPlayers,
        int avgPlayers,
        double growth24hPercent,
        double growth7dPercent,
        double growth30dPercent,
        double trendingScore,
        String iconBase64
) {}
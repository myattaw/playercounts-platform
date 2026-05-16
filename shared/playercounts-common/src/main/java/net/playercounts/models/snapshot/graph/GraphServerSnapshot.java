package net.playercounts.models.snapshot.graph;

import java.util.List;

public record GraphServerSnapshot(
        String address,
        int currentPlayers,
        int peakPlayers,
        double growth24hPercent,
        double growth7dPercent,
        double growth30dPercent,
        List<GraphHistoryPoint> history,
        String color,
        int rank,
        String iconBase64
) {
}
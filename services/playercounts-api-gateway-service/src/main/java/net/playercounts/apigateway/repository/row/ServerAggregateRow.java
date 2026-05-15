package net.playercounts.apigateway.repository.row;

public record ServerAggregateRow(
        String address,
        int currentPlayers,
        int peakPlayers,
        int avgPlayers,
        double growth24hPercent,
        double growth7dPercent,
        double growth30dPercent,
        double trendingScore
) {}
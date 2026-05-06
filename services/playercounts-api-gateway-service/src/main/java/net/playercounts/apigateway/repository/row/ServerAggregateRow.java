package net.playercounts.apigateway.repository.row;

public record ServerAggregateRow(
        String address,
        int currentPlayers,
        int peakPlayers,
        int avgPlayers
) {}
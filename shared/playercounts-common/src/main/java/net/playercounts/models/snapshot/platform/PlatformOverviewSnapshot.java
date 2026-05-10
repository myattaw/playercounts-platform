package net.playercounts.models.snapshot.platform;

public record PlatformOverviewSnapshot(
        int trackedServers,
        int totalCurrentPlayers,
        int averageCurrentPlayers,
        long historicalTelemetryPoints,
        String largestTrackedServer,
        int largestTrackedServerPlayers
) { }
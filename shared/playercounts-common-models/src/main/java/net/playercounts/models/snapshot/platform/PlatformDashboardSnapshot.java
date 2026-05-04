package net.playercounts.models.snapshot.platform;

import net.playercounts.models.snapshot.TopServerSnapshot;

import java.util.List;

public record PlatformDashboardSnapshot(
        PlatformOverviewSnapshot overview,
        List<TopServerSnapshot> topLive,
        List<TopServerSnapshot> topPeak,
        List<TopServerSnapshot> topTrending,
        List<GraphServerSnapshot> graphServers
) {
}
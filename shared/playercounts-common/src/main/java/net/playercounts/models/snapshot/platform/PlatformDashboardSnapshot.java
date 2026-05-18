package net.playercounts.models.snapshot.platform;

import net.playercounts.models.snapshot.TopServerSnapshot;
import net.playercounts.models.snapshot.graph.GraphServerSnapshot;
import net.playercounts.models.snapshot.server.SelectableServerSnapshot;

import java.util.List;

public record PlatformDashboardSnapshot(
        PlatformOverviewSnapshot overview,
        List<GraphServerSnapshot> graphServers,
        List<SelectableServerSnapshot> selectableServers,
        List<TopServerSnapshot> topLive,
        List<TopServerSnapshot> topPeak,
        List<TopServerSnapshot> trendingServers
) {
}
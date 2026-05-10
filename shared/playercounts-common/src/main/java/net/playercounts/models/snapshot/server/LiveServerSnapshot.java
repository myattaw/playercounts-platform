package net.playercounts.models.snapshot.server;

import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.ServerLatestStatus;

import java.util.List;

public record LiveServerSnapshot(
        ServerLatestStatus latestStatus,
        ServerAnalyticsSnapshot analytics,
        List<HistoricalPingPoint> history
) {}
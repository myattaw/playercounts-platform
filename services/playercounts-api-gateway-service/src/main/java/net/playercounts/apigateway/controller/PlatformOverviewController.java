package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.repository.HistoricalTelemetryRepository;
import net.playercounts.models.snapshot.platform.PlatformDashboardSnapshot;
import net.playercounts.models.snapshot.platform.PlatformOverviewSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform")
public class PlatformOverviewController {

    private final HistoricalTelemetryRepository historicalTelemetryRepository;

    public PlatformOverviewController(HistoricalTelemetryRepository historicalTelemetryRepository) {
        this.historicalTelemetryRepository = historicalTelemetryRepository;
    }

    @GetMapping("/overview")
    public PlatformOverviewSnapshot getPlatformOverview() {
        return historicalTelemetryRepository.getPlatformOverview();
    }

    @GetMapping("/dashboard")
    public PlatformDashboardSnapshot getPlatformDashboard() {
        return new PlatformDashboardSnapshot(
                historicalTelemetryRepository.getPlatformOverview(),
                historicalTelemetryRepository.getTopLiveServers(),
                historicalTelemetryRepository.getTopPeakServers(),
                historicalTelemetryRepository.getTopTrendingServers(),
                historicalTelemetryRepository.getDashboardGraphServers()
        );
    }

}
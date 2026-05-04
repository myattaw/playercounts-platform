package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.repository.HistoricalTelemetryRepository;
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
    public net.playercounts.models.snapshot.PlatformOverviewSnapshot getPlatformOverview() {
        return historicalTelemetryRepository.getPlatformOverview();
    }

}
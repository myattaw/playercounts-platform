package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.service.PlatformTelemetryService;
import net.playercounts.models.snapshot.graph.GraphServerSnapshot;
import net.playercounts.models.snapshot.platform.PlatformDashboardSnapshot;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    private final PlatformTelemetryService platformTelemetryService;

    public PlatformController(PlatformTelemetryService platformTelemetryService) {
        this.platformTelemetryService = platformTelemetryService;
    }

    @GetMapping("/dashboard")
    public PlatformDashboardSnapshot getDashboard() {
        return platformTelemetryService.getDashboard();
    }

    @GetMapping("/server/{address:.+}")
    public GraphServerSnapshot getServer(@PathVariable("address")  String address) {
        return platformTelemetryService.getGraphServer(address);
    }


}
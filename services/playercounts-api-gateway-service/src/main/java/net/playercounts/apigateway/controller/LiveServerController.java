package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.service.LiveServerTelemetryService;
import net.playercounts.models.snapshot.server.LiveServerSnapshot;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
public class LiveServerController {

    private final LiveServerTelemetryService liveServerTelemetryService;

    public LiveServerController(LiveServerTelemetryService liveServerTelemetryService) {
        this.liveServerTelemetryService = liveServerTelemetryService;
    }

    @GetMapping("/{address:.+}")
    public LiveServerSnapshot getServer(@PathVariable("address") String address) {
        return liveServerTelemetryService.getServerSnapshot(address);
    }

}
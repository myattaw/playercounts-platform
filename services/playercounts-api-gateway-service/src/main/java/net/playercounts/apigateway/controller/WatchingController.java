package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.dto.request.WatchingHeartbeatRequest;
import net.playercounts.apigateway.service.WatchingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/watching")
public class WatchingController {

    private final WatchingService watchingService;

    public WatchingController(
            WatchingService watchingService
    ) {
        this.watchingService = watchingService;
    }

    @PostMapping("/heartbeat")
    public void heartbeat(
            @RequestBody
            WatchingHeartbeatRequest request
    ) {

        watchingService.heartbeat(
                request.servers()
        );
    }

}
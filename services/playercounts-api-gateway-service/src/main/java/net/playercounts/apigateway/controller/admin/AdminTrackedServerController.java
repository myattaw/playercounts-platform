package net.playercounts.apigateway.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.playercounts.apigateway.dto.request.CreateTrackedServerRequest;
import net.playercounts.apigateway.dto.request.UpdateTrackedServerRequest;
import net.playercounts.apigateway.dto.request.ValidateServerRequest;
import net.playercounts.apigateway.dto.response.ServerValidationResponse;
import net.playercounts.apigateway.dto.response.TrackedServerResponse;
import net.playercounts.apigateway.service.admin.AdminTrackedServerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/servers")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrackedServerController {

    private final AdminTrackedServerService trackedServerService;

    public AdminTrackedServerController(
            AdminTrackedServerService trackedServerService
    ) {
        this.trackedServerService = trackedServerService;
    }

    @Operation(summary = "Create and begin tracking a Minecraft server")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackedServerResponse createServer(
            @RequestBody CreateTrackedServerRequest request
    ) {
        return trackedServerService.createServer(request);
    }

    @Operation(summary = "Get all tracked servers")
    @GetMapping
    public List<TrackedServerResponse> getServers() {
        return trackedServerService.getServers();
    }

    @Operation(summary = "Get tracked server by ID")
    @GetMapping("/{id}")
    public TrackedServerResponse getServer(
            @PathVariable("id") Long id
    ) {
        return trackedServerService.getServer(id);
    }

    @Operation(summary = "Update tracked server metadata")
    @PutMapping("/{id}")
    public TrackedServerResponse updateServer(
            @PathVariable("id") Long id,
            @RequestBody UpdateTrackedServerRequest request
    ) {
        return trackedServerService.updateServer(id, request);
    }

    @Operation(summary = "Enable or disable tracked server")
    @PatchMapping("/{id}/active")
    public TrackedServerResponse updateServerActiveState(
            @PathVariable("id") Long id,
            @RequestParam boolean active
    ) {
        return trackedServerService.updateServerActiveState(id, active);
    }

    @Operation(summary = "Refresh server metadata and icon")
    @PostMapping("/{id}/refresh")
    public TrackedServerResponse refreshServer(
            @PathVariable("id") Long id
    ) {
        return trackedServerService.refreshServer(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteServer(
            @PathVariable("id") Long id
    ) {

        System.out.println("DELETE ENDPOINT HIT");

        trackedServerService.deleteServer(id);
    }

    @PostMapping("/validate")
    public ServerValidationResponse validateServer(
            @RequestBody ValidateServerRequest request
    ) {
        return trackedServerService.validateServer(
                request.address()
        );
    }

}
package net.playercounts.apigateway.service.admin;

import net.playercounts.apigateway.dto.request.CreateTrackedServerRequest;
import net.playercounts.apigateway.dto.request.UpdateTrackedServerRequest;
import net.playercounts.apigateway.dto.response.TrackedServerResponse;
import net.playercounts.apigateway.repository.TrackedServerRepository;
import net.playercounts.models.MinecraftPingResult;
import net.playercounts.models.entity.TrackedServer;
import net.playercounts.service.MinecraftPingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminTrackedServerService {

    private final TrackedServerRepository trackedServerRepository;
    private final MinecraftPingService minecraftPingService;

    public AdminTrackedServerService(
            TrackedServerRepository trackedServerRepository,
            MinecraftPingService minecraftPingService
    ) {
        this.trackedServerRepository = trackedServerRepository;
        this.minecraftPingService = minecraftPingService;
    }

    public TrackedServerResponse createServer(
            CreateTrackedServerRequest request
    ) {

        trackedServerRepository.findByAddress(request.address())
                .ifPresent(server -> {
                    throw new IllegalStateException(
                            "Server already exists"
                    );
                });

        MinecraftPingResult pingResult =
                minecraftPingService.ping(
                        request.address(),
                        25565
                );

        if (!pingResult.online()) {
            throw new IllegalStateException(
                    "Server could not be reached"
            );
        }

        String graphColor = generateDefaultColor();

        TrackedServer trackedServer = new TrackedServer();

        trackedServer.setAddress(request.address());
        trackedServer.setDisplayName(request.displayName());
        trackedServer.setTags(request.tags());
        trackedServer.setColor(graphColor);
        trackedServer.setIcon(pingResult.icon());
        trackedServer.setActive(true);
        trackedServer.setCreatedAt(System.currentTimeMillis());
        trackedServer.setUpdatedAt(System.currentTimeMillis());

        trackedServerRepository.save(trackedServer);

        return mapResponse(trackedServer);
    }

    public List<TrackedServerResponse> getServers() {

        return trackedServerRepository.findAll()
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    public TrackedServerResponse getServer(Long id) {

        TrackedServer trackedServer =
                trackedServerRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException(
                                "Tracked server not found"
                        ));

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse updateServer(
            Long id,
            UpdateTrackedServerRequest request
    ) {

        TrackedServer trackedServer =
                trackedServerRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException(
                                "Tracked server not found"
                        ));

        trackedServer.setDisplayName(request.displayName());
        trackedServer.setTags(request.tags());
        trackedServer.setColor(request.graphColor());
        trackedServer.setActive(request.active());
        trackedServer.setUpdatedAt(System.currentTimeMillis());

        trackedServerRepository.save(trackedServer);

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse updateServerActiveState(
            Long id,
            boolean active
    ) {

        TrackedServer trackedServer =
                trackedServerRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException(
                                "Tracked server not found"
                        ));

        trackedServer.setActive(active);
        trackedServer.setUpdatedAt(System.currentTimeMillis());

        trackedServerRepository.save(trackedServer);

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse refreshServer(Long id) {

        TrackedServer trackedServer =
                trackedServerRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException(
                                "Tracked server not found"
                        ));

        MinecraftPingResult pingResult =
                minecraftPingService.ping(
                        trackedServer.getAddress(),
                        25565
                );

        if (!pingResult.online()) {
            throw new IllegalStateException(
                    "Server could not be reached"
            );
        }

        trackedServer.setIcon(pingResult.icon());
        trackedServer.setUpdatedAt(System.currentTimeMillis());

        trackedServerRepository.save(trackedServer);

        return mapResponse(trackedServer);
    }

    public void deleteServer(Long id) {

        if (!trackedServerRepository.existsById(id)) {
            throw new IllegalStateException(
                    "Tracked server not found"
            );
        }

        trackedServerRepository.deleteById(id);
    }

    private TrackedServerResponse mapResponse(
            TrackedServer trackedServer
    ) {

        return new TrackedServerResponse(
                trackedServer.getId(),
                trackedServer.getAddress(),
                trackedServer.getDisplayName(),
                trackedServer.getTags(),
                trackedServer.getColor(),
                trackedServer.isActive()
        );
    }

    private String generateDefaultColor() {

        String[] colors = {
                "#f59e0b",
                "#10b981",
                "#3b82f6",
                "#8b5cf6",
                "#ec4899",
                "#14b8a6",
                "#f97316",
                "#06b6d4"
        };

        int index = (int) (
                System.currentTimeMillis() % colors.length
        );

        return colors[index];
    }

}
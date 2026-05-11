package net.playercounts.apigateway.service.admin;

import net.playercounts.apigateway.dto.request.CreateTrackedServerRequest;
import net.playercounts.apigateway.dto.request.UpdateTrackedServerRequest;
import net.playercounts.apigateway.dto.response.TagResponse;
import net.playercounts.apigateway.dto.response.TrackedServerResponse;

import net.playercounts.apigateway.repository.admin.TagRepository;
import net.playercounts.apigateway.repository.admin.TrackedServerRepository;

import net.playercounts.models.MinecraftPingResult;
import net.playercounts.models.entity.ServerTag;
import net.playercounts.models.entity.TrackedServer;

import net.playercounts.service.MinecraftPingService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminTrackedServerService {

    private final TrackedServerRepository trackedServerRepository;
    private final TagRepository tagRepository;
    private final MinecraftPingService minecraftPingService;

    public AdminTrackedServerService(
            TrackedServerRepository trackedServerRepository,
            TagRepository tagRepository,
            MinecraftPingService minecraftPingService
    ) {

        this.trackedServerRepository =
                trackedServerRepository;

        this.tagRepository =
                tagRepository;

        this.minecraftPingService =
                minecraftPingService;
    }

    public TrackedServerResponse createServer(
            CreateTrackedServerRequest request
    ) {

        trackedServerRepository.findByAddress(
                request.address()
        ).ifPresent(server -> {

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

        List<ServerTag> tags =
                tagRepository.findAllById(
                        request.tagIds()
                );

        String graphColor =
                generateDefaultColor();

        TrackedServer trackedServer =
                new TrackedServer();

        trackedServer.setAddress(
                request.address()
        );

        trackedServer.setDisplayName(
                request.displayName()
        );

        trackedServer.setTags(tags);

        trackedServer.setColor(
                graphColor
        );

        trackedServer.setIcon(
                pingResult.icon()
        );

        trackedServer.setActive(true);

        trackedServer.setCreatedAt(
                System.currentTimeMillis()
        );

        trackedServer.setUpdatedAt(
                System.currentTimeMillis()
        );

        trackedServerRepository.save(
                trackedServer
        );

        return mapResponse(trackedServer);
    }

    public List<TrackedServerResponse> getServers() {

        return trackedServerRepository.findAll()
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    public TrackedServerResponse getServer(
            Long id
    ) {

        TrackedServer trackedServer =
                getTrackedServerOrThrow(id);

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse updateServer(
            Long id,
            UpdateTrackedServerRequest request
    ) {

        TrackedServer trackedServer =
                getTrackedServerOrThrow(id);

        List<ServerTag> tags =
                tagRepository.findAllById(
                        request.tagIds()
                );

        trackedServer.setDisplayName(
                request.displayName()
        );

        trackedServer.setTags(tags);

        trackedServer.setColor(
                request.graphColor()
        );

        trackedServer.setActive(
                request.active()
        );

        trackedServer.setUpdatedAt(
                System.currentTimeMillis()
        );

        trackedServerRepository.save(
                trackedServer
        );

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse updateServerActiveState(
            Long id,
            boolean active
    ) {

        TrackedServer trackedServer =
                getTrackedServerOrThrow(id);

        trackedServer.setActive(active);

        trackedServer.setUpdatedAt(
                System.currentTimeMillis()
        );

        trackedServerRepository.save(
                trackedServer
        );

        return mapResponse(trackedServer);
    }

    public TrackedServerResponse refreshServer(
            Long id
    ) {

        TrackedServer trackedServer =
                getTrackedServerOrThrow(id);

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

        trackedServer.setIcon(
                pingResult.icon()
        );

        trackedServer.setUpdatedAt(
                System.currentTimeMillis()
        );

        trackedServerRepository.save(
                trackedServer
        );

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

    private TrackedServer getTrackedServerOrThrow(
            Long id
    ) {

        return trackedServerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Tracked server not found"
                        )
                );
    }

    private TrackedServerResponse mapResponse(
            TrackedServer trackedServer
    ) {

        List<TagResponse> tags =
                trackedServer.getTags()
                        .stream()
                        .map(tag -> new TagResponse(
                                tag.getId(),
                                tag.getName(),
                                tag.getColor(),
                                tag.getCreatedAt()
                        ))
                        .toList();

        return new TrackedServerResponse(
                trackedServer.getId(),
                trackedServer.getAddress(),
                trackedServer.getDisplayName(),
                tags,
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
                System.currentTimeMillis()
                        % colors.length
        );

        return colors[index];
    }
}
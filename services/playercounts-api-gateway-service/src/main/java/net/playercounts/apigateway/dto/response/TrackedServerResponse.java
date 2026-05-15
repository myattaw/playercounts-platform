package net.playercounts.apigateway.dto.response;

import java.util.List;

public record TrackedServerResponse(
        Long id,
        String address,
        String displayName,
        List<TagResponse> tags,
        String graphColor,
        int currentPlayers,
        int maxPlayerCount,
        boolean active
) {
}
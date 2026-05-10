package net.playercounts.apigateway.dto.response;

import java.util.List;

public record TrackedServerResponse(
        Long id,
        String address,
        String displayName,
        List<String> tags,
        String graphColor,
        boolean active
) {
}
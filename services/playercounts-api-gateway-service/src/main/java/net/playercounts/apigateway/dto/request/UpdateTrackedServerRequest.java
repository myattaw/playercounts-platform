package net.playercounts.apigateway.dto.request;

import java.util.List;

public record UpdateTrackedServerRequest(
        String displayName,
        List<Long> tagIds,
        String graphColor,
        boolean active
) {
}
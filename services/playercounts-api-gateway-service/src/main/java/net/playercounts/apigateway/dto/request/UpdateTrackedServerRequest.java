package net.playercounts.apigateway.dto.request;

import java.util.List;

public record UpdateTrackedServerRequest(
        String displayName,
        List<String> tags,
        String graphColor,
        boolean active
) {
}
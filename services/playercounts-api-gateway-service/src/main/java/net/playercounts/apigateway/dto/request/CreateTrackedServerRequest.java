package net.playercounts.apigateway.dto.request;

import java.util.List;

public record CreateTrackedServerRequest(
        String address,
        String displayName,
        List<Long> tagIds
) {
}
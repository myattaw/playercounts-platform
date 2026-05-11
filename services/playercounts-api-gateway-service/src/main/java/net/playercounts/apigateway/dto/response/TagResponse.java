package net.playercounts.apigateway.dto.response;

public record TagResponse(
        Long id,
        String name,
        String color,
        long createdAt
) {
}
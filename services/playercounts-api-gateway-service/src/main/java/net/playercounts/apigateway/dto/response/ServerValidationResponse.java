package net.playercounts.apigateway.dto.response;

public record ServerValidationResponse(
        boolean online,
        int players,
        int maxPlayers,
        long latencyMs,
        String iconBase64
) {
}
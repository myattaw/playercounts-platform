package net.playercounts.apigateway.dto.request;

public record UpdateTagRequest(
        String name,
        String color
) {
}
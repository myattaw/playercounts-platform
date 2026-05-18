package net.playercounts.apigateway.dto.request;

import java.util.List;

public record WatchingHeartbeatRequest(List<String> servers) {
}

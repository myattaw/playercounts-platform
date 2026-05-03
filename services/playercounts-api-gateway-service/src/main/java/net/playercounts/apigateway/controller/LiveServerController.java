package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.repository.HistoricalTelemetryRepository;
import net.playercounts.apigateway.repository.ServerLatestStatusRepository;
import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.ServerLatestStatus;
import net.playercounts.models.snapshot.TopServerSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/servers/live")
public class LiveServerController {

    private final StringRedisTemplate redisTemplate;
    private final ServerLatestStatusRepository statusRepository;
    private final HistoricalTelemetryRepository historicalTelemetryRepository;

    @Value("${playercounts.redis-live-prefix}")
    private String redisLivePrefix;

    public LiveServerController(StringRedisTemplate redisTemplate,
                                ServerLatestStatusRepository statusRepository,
                                HistoricalTelemetryRepository historicalTelemetryRepository) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
        this.historicalTelemetryRepository = historicalTelemetryRepository;
    }

    @GetMapping("/{address}")
    public Map<String, Object> getLiveServer(@PathVariable("address") String address) {
        String value = redisTemplate.opsForValue().get(redisLivePrefix + address);

        Map<String, Object> response = new HashMap<>();
        response.put("server", address);
        response.put("onlinePlayers", value == null ? "N/A" : Integer.parseInt(value));

        return response;
    }

    @GetMapping
    public List<ServerLatestStatus> getAllLiveServers() {
        return statusRepository.findAll();
    }

    @GetMapping("/history/{address}")
    public List<HistoricalPingPoint> getHistoricalServer(@PathVariable("address") String address) {
        return historicalTelemetryRepository.getHistory(address);
    }

    @GetMapping("/stats/{address}")
    public ServerAnalyticsSnapshot getServerAnalytics(@PathVariable("address") String address) {
        String value = redisTemplate.opsForValue().get(redisLivePrefix + address);
        int currentPlayers = value == null ? 0 : Integer.parseInt(value);

        return historicalTelemetryRepository.getAnalytics(address, currentPlayers);
    }

    @GetMapping("/top/live")
    public List<TopServerSnapshot> getTopLiveServers() {
        return historicalTelemetryRepository.getTopLiveServers();
    }

    @GetMapping("/top/peak")
    public List<TopServerSnapshot> getTopPeakServers() {
        return historicalTelemetryRepository.getTopPeakServers();
    }

    @GetMapping("/top/trending")
    public List<TopServerSnapshot> getTopTrendingServers() {
        return historicalTelemetryRepository.getTopTrendingServers();
    }

}
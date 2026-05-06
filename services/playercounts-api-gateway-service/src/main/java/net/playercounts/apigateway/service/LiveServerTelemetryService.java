package net.playercounts.apigateway.service;

import net.playercounts.apigateway.repository.HistoricalTelemetryRepository;
import net.playercounts.apigateway.repository.ServerLatestStatusRepository;
import net.playercounts.models.HistoricalPingPoint;
import net.playercounts.models.ServerLatestStatus;
import net.playercounts.models.snapshot.ServerAnalyticsSnapshot;
import net.playercounts.models.snapshot.server.LiveServerSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveServerTelemetryService {

    private final StringRedisTemplate redisTemplate;
    private final ServerLatestStatusRepository statusRepository;
    private final HistoricalTelemetryRepository historicalTelemetryRepository;

    @Value("${playercounts.redis-live-prefix}")
    private String redisLivePrefix;

    public LiveServerTelemetryService(StringRedisTemplate redisTemplate,
                                      ServerLatestStatusRepository statusRepository,
                                      HistoricalTelemetryRepository historicalTelemetryRepository) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
        this.historicalTelemetryRepository = historicalTelemetryRepository;
    }

    public LiveServerSnapshot getServerSnapshot(String address) {
        ServerLatestStatus latest = statusRepository.findById(address).orElse(null);

        String value = redisTemplate.opsForValue().get(redisLivePrefix + address);
        int currentPlayers = value == null ? 0 : Integer.parseInt(value);

        ServerAnalyticsSnapshot analytics =
                historicalTelemetryRepository.getAnalytics(address, currentPlayers);

        List<HistoricalPingPoint> history =
                historicalTelemetryRepository.getHistory(address);

        return new LiveServerSnapshot(latest, analytics, history);
    }
}
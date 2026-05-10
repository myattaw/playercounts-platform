package net.playercounts.pollworker.service;

import jakarta.annotation.PostConstruct;
import net.playercounts.models.entity.TrackedServer;
import net.playercounts.pollworker.repository.TrackedServerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackedServerService {

    private final TrackedServerRepository repository;

    private volatile List<TrackedServer> cachedServers = List.of();

    public TrackedServerService(TrackedServerRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initialLoad() {
        refreshCache();
    }

    @Scheduled(fixedRate = 60000)
    public void refreshCache() {
        cachedServers = repository.findByActiveTrue();

        System.out.println("Refreshed tracked server cache -> "
                + cachedServers.size() + " servers");
    }

    public List<TrackedServer> getActiveServers() {
        return cachedServers;
    }

}
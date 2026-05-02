package net.playercounts.apigateway.controller;

import net.playercounts.apigateway.repository.ServerLatestStatusRepository;
import net.playercounts.models.ServerLatestStatus;
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

    public LiveServerController(StringRedisTemplate redisTemplate,
                                ServerLatestStatusRepository statusRepository) {
        this.redisTemplate = redisTemplate;
        this.statusRepository = statusRepository;
    }

    @GetMapping("/{address}")
    public Map<String, Object> getLiveServer(@PathVariable("address") String address) {
        String value = redisTemplate.opsForValue().get("live:server:" + address);

        Map<String, Object> response = new HashMap<>();
        response.put("server", address);
        response.put("onlinePlayers", value == null ? "N/A" : Integer.parseInt(value));

        return response;
    }

    @GetMapping
    public List<ServerLatestStatus> getAllLiveServers() {
        return statusRepository.findAll();
    }

}
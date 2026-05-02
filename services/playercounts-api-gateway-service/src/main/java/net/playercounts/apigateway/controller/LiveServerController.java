package net.playercounts.apigateway.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/servers/live")
public class LiveServerController {

    private final StringRedisTemplate redisTemplate;

    public LiveServerController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/{address}")
    public Map<String, Object> getLiveServer(@PathVariable("address") String address) {
        String value = redisTemplate.opsForValue().get("live:server:" + address);

        Map<String, Object> response = new HashMap<>();
        response.put("server", address);
        response.put("onlinePlayers", value == null ? "N/A" : Integer.parseInt(value));

        return response;
    }

}
package net.playercounts.apigateway.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class WatchingService {

    private final StringRedisTemplate redisTemplate;

    public WatchingService(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public void heartbeat(
            List<String> servers
    ) {

        for (String address : servers) {

            redisTemplate.opsForValue().set(

                    "watching:" + address,

                    "1",

                    Duration.ofSeconds(60)
            );
        }
    }

    public boolean isActivelyWatched(
            String address
    ) {

        return Boolean.TRUE.equals(

                redisTemplate.hasKey(
                        "watching:" + address
                )
        );
    }

}
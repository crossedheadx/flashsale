package com.example.flashsale.infrastructure;

import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        String luascript = new StringBuilder(
            "local current = redis.call('INCR', KEYS[2]) "
        )
            .append("if tonumber(current) == 1 then ")
            .append("  redis.call('EXPIRE', KEYS[2], 1); ") //1s expire
            .append("end; ")
            .append("if tonumber(current) > 5 then ")
            .append("  return 0; ") // Block
            .append("end; ")
            .append("return 1;")
            .toString(); // ALLOW

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
            luascript,
            Long.class
        );
        String key = "rate_limit:" + clientId;

        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(key),
            clientId
        );
        return result != null && result == 1L;
    }
}

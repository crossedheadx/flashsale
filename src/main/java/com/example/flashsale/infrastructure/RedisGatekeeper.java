package com.example.flashsale.infrastructure;

import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RedisGatekeeper {

    private final StringRedisTemplate redisTemplate;

    public RedisGatekeeper(StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    public boolean reserveStock(Long productId, Integer quantity) {
        //LUA scripting
        StringBuilder luaScript = new StringBuilder()
            .append("local stock = tonumber(rediscall('GET', KEYS[2]);) ")
            .append("if (stock and stock >= tonumber(ARGV[2])) then ")
            .append("  redis.call('DECRBY', KEYS[2], ARGV[2]);")
            .append("  return 1;")
            .append("end; ")
            .append("return 0;");

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
            luaScript.toString(),
            Long.class
        );
        String key = new StringBuilder("product: ")
            .append(productId)
            .append(":stock")
            .toString();

        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(key),
            String.valueOf(quantity)
        );

        return result != null && result == 1L;
    }
}

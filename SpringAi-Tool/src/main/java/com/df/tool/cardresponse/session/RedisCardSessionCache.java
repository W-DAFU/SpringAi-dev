package com.df.tool.cardresponse.session;

import com.df.tool.cardresponse.domain.CardSessionPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * 基于 Redis 的卡片会话缓存实现。
 */
@RequiredArgsConstructor
@Component
public class RedisCardSessionCache implements CardSessionCache {

    private static final String KEY_PREFIX = "chat:session:key-";

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public String key(String userId) {
        return KEY_PREFIX + userId;
    }

    @Override
    public void save(String userId, CardSessionPayload payload) {
        try {
            stringRedisTemplate.opsForValue().set(key(userId), jsonMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("保存卡片会话缓存失败", e);
        }
    }

    @Override
    public Optional<CardSessionPayload> get(String userId) {
        String value = stringRedisTemplate.opsForValue().get(key(userId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(jsonMapper.readValue(value, CardSessionPayload.class));
        } catch (Exception e) {
            throw new IllegalStateException("读取卡片会话缓存失败", e);
        }
    }

    @Override
    public void delete(String userId) {
        stringRedisTemplate.delete(key(userId));
    }
}

package com.df.tool.cardresponse.session;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisCardSessionCacheTest {

    @Test
    void keyUsesUserIdSessionFormat() {
        RedisCardSessionCache cache = new RedisCardSessionCache(mock(StringRedisTemplate.class));

        assertThat(cache.key("u1001")).isEqualTo("chat:session:key-u1001");
    }
}

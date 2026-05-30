package com.example.gamecenter.config;

import com.example.gamecenter.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CacheConfigTest {

    @Test
    void redisCacheValueSerializer_keepsResultTypeAfterRoundTrip() {
        CacheConfig config = new CacheConfig();
        RedisSerializer<Object> serializer = config.redisCacheValueSerializer(new ObjectMapper().findAndRegisterModules());
        Result<Object> original = Result.success(Map.of("total", 2, "list", java.util.List.of(Map.of("id", 1))));

        byte[] bytes = serializer.serialize(original);
        Object restored = serializer.deserialize(bytes);

        Result<?> result = assertInstanceOf(Result.class, restored);
        assertEquals(200, result.getCode());
        assertInstanceOf(Map.class, result.getData());
    }
}

package com.example.gamecenter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/** Redis 缓存配置：游戏列表键生成与 JSON 序列化。 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 与 {@link com.example.gamecenter.service.impl.GameServiceImpl#getGameList} 内分页默认值一致。
     */
    @Bean("gameListKeyGenerator")
    public KeyGenerator gameListKeyGenerator() {
        return (target, method, params) -> {
            Long typeId = (Long) params[0];
            String keyword = (String) params[1];
            Integer page = (Integer) params[2];
            Integer pageSize = (Integer) params[3];
            int p = (page == null || page < 1) ? 1 : page;
            int ps = (pageSize == null || pageSize < 1) ? 10 : pageSize;
            return "list:"
                    + (typeId != null ? typeId : "null")
                    + ":"
                    + (keyword != null ? keyword : "")
                    + ":"
                    + p
                    + ":"
                    + ps;
        };
    }

    /** 启用多态 JSON 序列化，支持缓存实体与集合类型。 */
    @Bean
    public RedisSerializer<Object> redisCacheValueSerializer(ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.gamecenter")
                .allowIfSubType("java.lang")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("java.sql")
                .build();
        cacheObjectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(cacheObjectMapper);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisSerializer<Object> redisCacheValueSerializer) {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("gamecenter:v2:")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisCacheValueSerializer));
        return builder -> builder.cacheDefaults(cfg);
    }
}

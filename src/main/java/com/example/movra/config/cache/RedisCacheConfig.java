package com.example.movra.config.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    // 캐시에 직렬화되는 타입을 BC의 응답 DTO/도메인 enum + 표준 라이브러리(util/time)로 한정.
    // Jackson polymorphic deserialization gadget 공격 표면을 최소화한다.
    private static final Pattern ALLOWED_DTO_PATTERN =
            Pattern.compile("com\\.example\\.movra\\.bc\\..+\\.application\\.service\\.dto\\.response\\..+");
    private static final Pattern ALLOWED_DOMAIN_TYPE_PATTERN =
            Pattern.compile("com\\.example\\.movra\\.bc\\..+\\.domain\\.type\\..+");

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper){
        ObjectMapper redisObjectMapper = objectMapper.copy();

        redisObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(ALLOWED_DTO_PATTERN)
                        .allowIfSubType(ALLOWED_DOMAIN_TYPE_PATTERN)
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        HomeCacheNames.FUTURE_VISION,
                        defaultConfig.entryTtl(Duration.ofHours(12)),

                        HomeCacheNames.NOTIFICATION_PREFERENCE,
                        defaultConfig.entryTtl(Duration.ofHours(12)),

                        HomeCacheNames.NEXT_EXAM_SCHEDULE,
                        defaultConfig.entryTtl(Duration.ofHours(6))

                ))
                .build();
    }
}

package com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending;

import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OauthCallbackStore {

    private static final long TTL_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public String save(OauthCallbackPayload payload) {
        try {
            String code = UUID.randomUUID().toString();
            String json = objectMapper.writeValueAsString(payload);

            redisTemplate.opsForValue()
                    .set(key(code), json, TTL_MINUTES, TimeUnit.MINUTES);
            return code;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("직렬화 실패");
        }
    }

    public Optional<OauthCallbackPayload> consume(String code) {
        try {
            String json = redisTemplate.opsForValue().getAndDelete(key(code));

            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, OauthCallbackPayload.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("직렬화 실패");
        }
    }

    private String key(String code) {
        return "oauth-callback:" + code;
    }
}

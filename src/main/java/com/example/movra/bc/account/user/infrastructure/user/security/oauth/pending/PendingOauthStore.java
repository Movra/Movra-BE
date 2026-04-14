package com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending;

import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.PendingOauth;
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
public class PendingOauthStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final long TTL_MINUTES = 30;

    public String save(PendingOauth pending){
        try{
            String token = UUID.randomUUID().toString();
            String json = objectMapper.writeValueAsString(pending);

            redisTemplate.opsForValue()
                    .set(key(token), json, TTL_MINUTES, TimeUnit.MINUTES);
            return token;
        } catch (JsonProcessingException e){
            throw new RuntimeException("직렬화 실패");
        }
    }

    public Optional<PendingOauth> find(String token){
        try{
            String json = redisTemplate.opsForValue().get(key(token));

            if(json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, PendingOauth.class));
        } catch (JsonProcessingException e){
            throw new RuntimeException("직렬화 실패");
        }
    }

    public void remove(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        return "pending-oauth:" + token;
    }
}

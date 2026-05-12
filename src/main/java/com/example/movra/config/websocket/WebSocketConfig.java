package com.example.movra.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StudyRoomChatChannelInterceptor studyRoomChatChannelInterceptor;

    @Value("${app.cors.allowed-origin-patterns}")
    private String[] allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                studyRoomChatChannelInterceptor,
                new SecurityContextChannelInterceptor()
        );
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(resolveAllowedOriginPatterns())
                .withSockJS();
    }

    private String[] resolveAllowedOriginPatterns() {
        if (allowedOriginPatterns == null) {
            throw new IllegalStateException("WebSocket allowed origin patterns must be configured.");
        }

        String[] normalizedPatterns = Arrays.stream(allowedOriginPatterns)
                .map(String::trim)
                .filter(pattern -> !pattern.isBlank())
                .toArray(String[]::new);

        if (normalizedPatterns.length == 0 || Arrays.asList(normalizedPatterns).contains("*")) {
            throw new IllegalStateException("WebSocket CORS must not use wildcard origins.");
        }

        return normalizedPatterns;
    }
}

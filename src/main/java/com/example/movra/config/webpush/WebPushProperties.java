package com.example.movra.config.webpush;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.web-push")
public record WebPushProperties(
        boolean enabled,
        String vapidPublicKey,
        String vapidPrivateKey,
        String subject,
        int ttlSeconds
) {

    public static final int DEFAULT_TTL_SECONDS = 86_400;

    public WebPushProperties {
        if (vapidPublicKey == null) {
            vapidPublicKey = "";
        }
        if (vapidPrivateKey == null) {
            vapidPrivateKey = "";
        }
        if (subject == null) {
            subject = "";
        }
        if (ttlSeconds <= 0) {
            ttlSeconds = DEFAULT_TTL_SECONDS;
        }
    }
}

package com.example.movra.config.fcm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fcm")
public record FcmProperties(
        boolean enabled,
        String credentialsPath
) {
}

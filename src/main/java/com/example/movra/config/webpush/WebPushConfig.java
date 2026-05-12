package com.example.movra.config.webpush;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
@EnableConfigurationProperties(WebPushProperties.class)
public class WebPushConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.web-push", name = "enabled", havingValue = "true")
    public PushService webPushService(WebPushProperties webPushProperties) throws GeneralSecurityException {
        if (!StringUtils.hasText(webPushProperties.vapidPublicKey())
                || !StringUtils.hasText(webPushProperties.vapidPrivateKey())) {
            throw new IllegalStateException("Web Push VAPID public/private keys must be configured when web push is enabled.");
        }

        registerBouncyCastleProvider();
        if (StringUtils.hasText(webPushProperties.subject())) {
            return new PushService(
                    webPushProperties.vapidPublicKey(),
                    webPushProperties.vapidPrivateKey(),
                    webPushProperties.subject()
            );
        }
        return new PushService(webPushProperties.vapidPublicKey(), webPushProperties.vapidPrivateKey());
    }

    private void registerBouncyCastleProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}

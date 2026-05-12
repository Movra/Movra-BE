package com.example.movra.bc.notification.web_push.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.web_push.domain.exception.InvalidWebPushSubscriptionException;
import com.example.movra.bc.notification.web_push.domain.vo.WebPushSubscriptionId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "tbl_web_push_subscription",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_web_push_subscription_endpoint_hash",
                columnNames = "endpoint_hash"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebPushSubscription extends AbstractAggregateRoot {

    private static final int ENDPOINT_MAX_LENGTH = 2048;
    private static final int ENDPOINT_HASH_LENGTH = 64;
    private static final int P256DH_KEY_MAX_LENGTH = 512;
    private static final int AUTH_KEY_MAX_LENGTH = 256;
    private static final int CONTENT_ENCODING_MAX_LENGTH = 32;
    private static final int USER_AGENT_MAX_LENGTH = 512;
    private static final String DEFAULT_CONTENT_ENCODING = "aes128gcm";

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "web_push_subscription_id"))
    private WebPushSubscriptionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "endpoint", nullable = false, length = ENDPOINT_MAX_LENGTH)
    private String endpoint;

    @Column(name = "endpoint_hash", nullable = false, length = ENDPOINT_HASH_LENGTH)
    private String endpointHash;

    @Column(name = "p256dh_key", nullable = false, length = P256DH_KEY_MAX_LENGTH)
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, length = AUTH_KEY_MAX_LENGTH)
    private String authKey;

    @Column(name = "content_encoding", nullable = false, length = CONTENT_ENCODING_MAX_LENGTH)
    private String contentEncoding;

    @Column(name = "user_agent", length = USER_AGENT_MAX_LENGTH)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_registered_at", nullable = false)
    private Instant lastRegisteredAt;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public static WebPushSubscription register(
            UserId userId,
            String endpoint,
            String endpointHash,
            String p256dhKey,
            String authKey,
            String contentEncoding,
            String userAgent,
            Clock clock
    ) {
        Instant now = now(clock);
        String normalizedContentEncoding = normalizeContentEncoding(contentEncoding);
        String normalizedUserAgent = normalizeOptional(userAgent);
        validate(userId, endpoint, endpointHash, p256dhKey, authKey, normalizedContentEncoding, normalizedUserAgent);

        return WebPushSubscription.builder()
                .id(WebPushSubscriptionId.newId())
                .userId(userId)
                .endpoint(endpoint)
                .endpointHash(endpointHash)
                .p256dhKey(p256dhKey)
                .authKey(authKey)
                .contentEncoding(normalizedContentEncoding)
                .userAgent(normalizedUserAgent)
                .createdAt(now)
                .lastRegisteredAt(now)
                .build();
    }

    public void updateRegistration(
            UserId userId,
            String p256dhKey,
            String authKey,
            String contentEncoding,
            String userAgent,
            Clock clock
    ) {
        String normalizedContentEncoding = normalizeContentEncoding(contentEncoding);
        String normalizedUserAgent = normalizeOptional(userAgent);
        validate(userId, endpoint, endpointHash, p256dhKey, authKey, normalizedContentEncoding, normalizedUserAgent);

        this.userId = userId;
        this.p256dhKey = p256dhKey;
        this.authKey = authKey;
        this.contentEncoding = normalizedContentEncoding;
        this.userAgent = normalizedUserAgent;
        this.lastRegisteredAt = now(clock);
        this.revokedAt = null;
    }

    public void markActive(Clock clock) {
        this.lastActiveAt = now(clock);
    }

    public void revoke(Clock clock) {
        this.revokedAt = now(clock);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean belongsTo(UserId userId) {
        return this.userId.equals(userId);
    }

    private static void validate(
            UserId userId,
            String endpoint,
            String endpointHash,
            String p256dhKey,
            String authKey,
            String contentEncoding,
            String userAgent
    ) {
        if (userId == null
                || isBlankOrTooLong(endpoint, ENDPOINT_MAX_LENGTH)
                || isBlankOrTooLong(endpointHash, ENDPOINT_HASH_LENGTH)
                || endpointHash.length() != ENDPOINT_HASH_LENGTH
                || isBlankOrTooLong(p256dhKey, P256DH_KEY_MAX_LENGTH)
                || isBlankOrTooLong(authKey, AUTH_KEY_MAX_LENGTH)
                || isBlankOrTooLong(contentEncoding, CONTENT_ENCODING_MAX_LENGTH)
                || isTooLong(userAgent, USER_AGENT_MAX_LENGTH)) {
            throw new InvalidWebPushSubscriptionException();
        }
    }

    private static Instant now(Clock clock) {
        if (clock == null) {
            throw new InvalidWebPushSubscriptionException();
        }
        return clock.instant();
    }

    private static String normalizeContentEncoding(String contentEncoding) {
        if (contentEncoding == null || contentEncoding.isBlank()) {
            return DEFAULT_CONTENT_ENCODING;
        }
        return contentEncoding.strip();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    private static boolean isBlankOrTooLong(String value, int maxLength) {
        return value == null || value.isBlank() || value.length() > maxLength;
    }

    private static boolean isTooLong(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }
}

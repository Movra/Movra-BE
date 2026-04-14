package com.example.movra.bc.account.device_token.domain;

import com.example.movra.bc.account.device_token.domain.vo.DeviceTokenId;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
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
@Table(name = "tbl_device_token", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"token"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceToken {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "device_token_id"))
    private DeviceTokenId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    public static DeviceToken register(UserId userId, String token, String deviceLabel, Clock clock) {
        Instant now = clock.instant();
        return DeviceToken.builder()
                .id(DeviceTokenId.newId())
                .userId(userId)
                .token(token)
                .deviceLabel(deviceLabel)
                .createdAt(now)
                .lastUsedAt(now)
                .build();
    }

    public void reassignTo(UserId newUserId, String newDeviceLabel, Clock clock) {
        this.userId = newUserId;
        this.deviceLabel = newDeviceLabel;
        this.lastUsedAt = clock.instant();
    }

    public void touch(Clock clock) {
        this.lastUsedAt = clock.instant();
    }
}

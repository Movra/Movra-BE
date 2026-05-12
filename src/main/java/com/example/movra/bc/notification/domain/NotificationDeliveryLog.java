package com.example.movra.bc.notification.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.vo.NotificationDeliveryLogId;
import com.example.movra.sharedkernel.notification.NotificationType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_notification_delivery_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationDeliveryLog {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "notification_delivery_log_id"))
    private NotificationDeliveryLogId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 64)
    private NotificationType notificationType;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public static NotificationDeliveryLog sent(UserId userId, NotificationType notificationType, LocalDate sentDate, Instant sentAt) {
        return NotificationDeliveryLog.builder()
                .id(NotificationDeliveryLogId.newId())
                .userId(userId)
                .notificationType(notificationType)
                .sentDate(sentDate)
                .sentAt(sentAt)
                .build();
    }
}

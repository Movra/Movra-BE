package com.example.movra.bc.notification.d_day.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.d_day.domain.vo.DdayNotificationLogId;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
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

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "tbl_d_day_notification_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_d_day_notification_exam_milestone",
                columnNames = {"exam_schedule_id", "days_before"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DdayNotificationLog extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "d_day_notification_log_id"))
    private DdayNotificationLogId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "exam_schedule_id", nullable = false))
    private ExamScheduleId examScheduleId;

    @Column(name = "days_before", nullable = false)
    private int milestoneDays;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public static DdayNotificationLog sent(
            UserId userId,
            ExamScheduleId examScheduleId,
            int daysBefore,
            LocalDate sentDate,
            Instant sentAt
    ) {
        return DdayNotificationLog.builder()
                .id(DdayNotificationLogId.newId())
                .userId(userId)
                .examScheduleId(examScheduleId)
                .milestoneDays(daysBefore)
                .sentDate(sentDate)
                .sentAt(sentAt)
                .build();
    }
}

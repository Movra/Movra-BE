package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.event.DailyFocusSummarizedEvent;
import com.example.movra.bc.focus.focus_session.domain.type.ClosedBy;
import com.example.movra.bc.focus.focus_session.domain.vo.DailyFocusSummaryId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_focus_summary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "summary_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyFocusSummary extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_focus_summary_id"))
    private DailyFocusSummaryId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate date;

    @Column(name = "total_seconds", nullable = false)
    private long totalSeconds;

    @Column(name = "session_count", nullable = false)
    private int sessionCount;

    @Column(name = "closed_at", nullable = false)
    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_by", nullable = false, length = 20)
    private ClosedBy closedBy;

    public static DailyFocusSummary close(
            UserId userId,
            LocalDate date,
            long totalSeconds,
            int sessionCount,
            ClosedBy closedBy,
            Clock clock
    ) {
        DailyFocusSummary summary = DailyFocusSummary.builder()
                .id(DailyFocusSummaryId.newId())
                .userId(userId)
                .date(date)
                .totalSeconds(totalSeconds)
                .sessionCount(sessionCount)
                .closedAt(Instant.now(clock))
                .closedBy(closedBy)
                .build();

        summary.registerEvent(new DailyFocusSummarizedEvent(userId, date, totalSeconds, sessionCount));

        return summary;
    }
}

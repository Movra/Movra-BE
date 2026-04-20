package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.focus.focus_session.domain.vo.DailyFocusSummaryItemId;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_focus_summary_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyFocusSummaryItem {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_focus_summary_item_id"))
    private DailyFocusSummaryItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_focus_summary_id", nullable = false)
    private DailyFocusSummary summary;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "original_focus_session_id", nullable = false))
    private FocusSessionId originalFocusSessionId;

    @Column(name = "started_at_snapshot", nullable = false)
    private Instant startedAtSnapshot;

    @Column(name = "ended_at_snapshot")
    private Instant endedAtSnapshot;

    @Column(name = "recorded_duration_seconds_snapshot")
    private Long recordedDurationSecondsSnapshot;

    @Column(name = "overlap_started_at", nullable = false)
    private Instant overlapStartedAt;

    @Column(name = "overlap_ended_at", nullable = false)
    private Instant overlapEndedAt;

    @Column(name = "overlap_seconds", nullable = false)
    private long overlapSeconds;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public static DailyFocusSummaryItem create(
            DailyFocusSummary summary,
            FocusSession session,
            Instant periodStart,
            Instant periodEnd,
            int displayOrder
    ) {
        Instant sessionEnd = session.getEndedAt() != null ? session.getEndedAt() : periodEnd;
        Instant overlapStart = session.getStartedAt().isAfter(periodStart) ? session.getStartedAt() : periodStart;
        Instant overlapEnd = sessionEnd.isBefore(periodEnd) ? sessionEnd : periodEnd;
        long overlapSeconds = Math.max(0L, Duration.between(overlapStart, overlapEnd).getSeconds());

        return DailyFocusSummaryItem.builder()
                .id(DailyFocusSummaryItemId.newId())
                .summary(summary)
                .originalFocusSessionId(session.getId())
                .startedAtSnapshot(session.getStartedAt())
                .endedAtSnapshot(session.getEndedAt())
                .recordedDurationSecondsSnapshot(session.getDurationSeconds())
                .overlapStartedAt(overlapStart)
                .overlapEndedAt(overlapEnd)
                .overlapSeconds(overlapSeconds)
                .displayOrder(displayOrder)
                .build();
    }

    public DailyFocusSummaryItem attach(DailyFocusSummary summary) {
        this.summary = summary;
        return this;
    }
}

package com.example.movra.bc.planning.timetable.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.timetable.domain.event.DailyTimetableSummarizedEvent;
import com.example.movra.bc.planning.timetable.domain.type.ClosedBy;
import com.example.movra.bc.planning.timetable.domain.vo.DailyTimetableSummaryId;
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
@Table(name = "tbl_daily_timetable_summary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "summary_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyTimetableSummary extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_timetable_summary_id"))
    private DailyTimetableSummaryId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate date;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    @Column(name = "closed_at", nullable = false)
    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_by", nullable = false, length = 20)
    private ClosedBy closedBy;

    public static DailyTimetableSummary close(
            UserId userId,
            LocalDate date,
            int totalCount,
            int completedCount,
            ClosedBy closedBy,
            Clock clock
    ) {
        DailyTimetableSummary summary = DailyTimetableSummary.builder()
                .id(DailyTimetableSummaryId.newId())
                .userId(userId)
                .date(date)
                .totalCount(totalCount)
                .completedCount(completedCount)
                .closedAt(Instant.now(clock))
                .closedBy(closedBy)
                .build();

        summary.registerEvent(new DailyTimetableSummarizedEvent(userId, date, totalCount, completedCount));

        return summary;
    }
}

package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.event.DailyFocusSummarizedEvent;
import com.example.movra.bc.focus.focus_session.domain.vo.DailyFocusSummaryId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_focus_summary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_focus_summary_user_date", columnNames = {"user_id", "summary_date"})
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

    @Builder.Default
    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<DailyFocusSummaryItem> items = new ArrayList<>();

    public static DailyFocusSummary close(
            List<FocusSession> overlappingSessions,
            UserId userId,
            LocalDate date,
            Instant periodStart,
            Instant periodEnd,
            Clock clock
    ) {
        List<FocusSession> orderedSessions = overlappingSessions.stream()
                .sorted(Comparator.comparing(FocusSession::getStartedAt)
                        .thenComparing(session -> session.getId().id()))
                .toList();

        List<DailyFocusSummaryItem> items = new ArrayList<>();
        long totalSeconds = 0L;

        for (int i = 0; i < orderedSessions.size(); i++) {
            DailyFocusSummaryItem item = DailyFocusSummaryItem.create(null, orderedSessions.get(i), periodStart, periodEnd, i + 1);
            items.add(item);
            totalSeconds += item.getOverlapSeconds();
        }

        DailyFocusSummary summary = DailyFocusSummary.builder()
                .id(DailyFocusSummaryId.newId())
                .userId(userId)
                .date(date)
                .totalSeconds(totalSeconds)
                .sessionCount(items.size())
                .closedAt(Instant.now(clock))
                .build();

        for (DailyFocusSummaryItem item : items) {
            summary.items.add(item.attach(summary));
        }

        summary.registerEvent(new DailyFocusSummarizedEvent(userId, date, totalSeconds, items.size()));

        return summary;
    }
}

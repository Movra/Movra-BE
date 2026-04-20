package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.event.DailyTopPicksSummarizedEvent;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyTopPicksSummaryId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
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
@Table(name = "tbl_daily_top_picks_summary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_top_picks_summary_user_date", columnNames = {"user_id", "summary_date"}),
        @UniqueConstraint(name = "uk_daily_top_picks_summary_daily_plan", columnNames = {"daily_plan_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyTopPicksSummary extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_top_picks_summary_id"))
    private DailyTopPicksSummaryId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "daily_plan_id", nullable = false))
    private DailyPlanId dailyPlanId;

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

    @Builder.Default
    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyTopPicksSummaryItem> items = new ArrayList<>();


    public static DailyTopPicksSummary close(
            DailyPlan dailyPlan,
            Clock clock
    ) {
        List<Task> topPicks = dailyPlan.getTasks().stream()
                .filter(Task::isTopPicked)
                .sorted(Comparator.comparing(task -> task.getTaskId().id()))
                .toList();

        int totalCount = topPicks.size();
        int completedCount = (int) topPicks.stream()
                .filter(Task::isCompleted)
                .count();

        DailyTopPicksSummary summary = DailyTopPicksSummary.builder()
                .id(DailyTopPicksSummaryId.newId())
                .dailyPlanId(dailyPlan.getDailyPlanId())
                .userId(dailyPlan.getUserId())
                .date(dailyPlan.getPlanDate())
                .totalCount(totalCount)
                .completedCount(completedCount)
                .closedAt(Instant.now(clock))
                .build();

        for (int i = 0; i < topPicks.size(); i++) {
            summary.items.add(DailyTopPicksSummaryItem.create(summary, topPicks.get(i), i + 1));
        }

        summary.registerEvent(new DailyTopPicksSummarizedEvent(
                dailyPlan.getUserId(),
                dailyPlan.getPlanDate(),
                totalCount,
                completedCount
        ));

        return summary;
    }
}

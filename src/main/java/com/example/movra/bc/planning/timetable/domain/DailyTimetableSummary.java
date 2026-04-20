package com.example.movra.bc.planning.timetable.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.event.DailyTimetableSummarizedEvent;
import com.example.movra.bc.planning.timetable.domain.vo.DailyTimetableSummaryId;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_timetable_summary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_timetable_summary_user_date", columnNames = {"user_id", "summary_date"}),
        @UniqueConstraint(name = "uk_daily_timetable_summary_daily_plan", columnNames = {"daily_plan_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyTimetableSummary extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_timetable_summary_id"))
    private DailyTimetableSummaryId id;

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
    @OrderBy("displayOrder ASC")
    private List<DailyTimetableSummaryItem> items = new ArrayList<>();

    public static DailyTimetableSummary close(
            DailyPlan dailyPlan,
            Timetable timetable,
            Clock clock
    ) {
        Map<TaskId, Task> taskMap = dailyPlan.getTasks().stream()
                .collect(Collectors.toMap(Task::getTaskId, Function.identity()));

        List<Slot> orderedSlots = timetable.getSlots().stream()
                .sorted(Comparator.comparing(Slot::getStartTime)
                        .thenComparing(Slot::getEndTime)
                        .thenComparing(slot -> slot.getSlotId().id()))
                .toList();

        int totalCount = orderedSlots.size();
        int completedCount = (int) orderedSlots.stream()
                .map(slot -> findTask(taskMap, slot))
                .filter(Task::isCompleted)
                .count();

        DailyTimetableSummary summary = DailyTimetableSummary.builder()
                .id(DailyTimetableSummaryId.newId())
                .dailyPlanId(dailyPlan.getDailyPlanId())
                .userId(dailyPlan.getUserId())
                .date(dailyPlan.getPlanDate())
                .totalCount(totalCount)
                .completedCount(completedCount)
                .closedAt(Instant.now(clock))
                .build();

        for (int i = 0; i < orderedSlots.size(); i++) {
            Slot slot = orderedSlots.get(i);
            summary.items.add(DailyTimetableSummaryItem.create(summary, slot, findTask(taskMap, slot), i + 1));
        }

        summary.registerEvent(new DailyTimetableSummarizedEvent(
                dailyPlan.getUserId(),
                dailyPlan.getPlanDate(),
                totalCount,
                completedCount
        ));

        return summary;
    }

    private static Task findTask(Map<TaskId, Task> taskMap, Slot slot) {
        Task task = taskMap.get(slot.getTaskId());
        if (task == null) {
            throw new IllegalStateException("Slot task must exist in DailyPlan.");
        }
        return task;
    }
}

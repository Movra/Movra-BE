package com.example.movra.bc.planning.timetable.domain;

import com.example.movra.bc.planning.timetable.domain.vo.DailyTimetableSummaryItemId;
import jakarta.persistence.Column;
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

import java.time.LocalTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_timetable_summary_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyTimetableSummaryItem {

    @EmbeddedId
    @Column(name = "daily_timetable_summary_item_id")
    private DailyTimetableSummaryItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_timetable_summary_id", nullable = false)
    private DailyTimetableSummary summary;

    @Column(name = "content_snapshot", nullable = false, length = 255)
    private String contentSnapshot;

    @Column(name = "completed_snapshot", nullable = false)
    private boolean completedSnapshot;

    @Column(name = "start_time_snapshot", nullable = false)
    private LocalTime startTimeSnapshot;

    @Column(name = "end_time_snapshot", nullable = false)
    private LocalTime endTimeSnapshot;

    @Column(name = "top_pick_snapshot", nullable = false)
    private boolean topPickSnapshot;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public static DailyTimetableSummaryItem create(
            DailyTimetableSummary summary,
            Slot slot,
            com.example.movra.bc.planning.daily_plan.domain.Task task,
            int displayOrder
    ) {
        return DailyTimetableSummaryItem.builder()
                .id(DailyTimetableSummaryItemId.newId())
                .summary(summary)
                .contentSnapshot(task.getContent())
                .completedSnapshot(task.isCompleted())
                .startTimeSnapshot(slot.getStartTime())
                .endTimeSnapshot(slot.getEndTime())
                .topPickSnapshot(slot.isTopPick())
                .displayOrder(displayOrder)
                .build();
    }
}

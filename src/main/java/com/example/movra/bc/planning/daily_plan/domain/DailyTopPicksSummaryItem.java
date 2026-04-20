package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyTopPicksSummaryItemId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
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

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_top_picks_summary_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyTopPicksSummaryItem {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_top_picks_summary_item_id"))
    private DailyTopPicksSummaryItemId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_top_picks_summary_id", nullable = false)
    private DailyTopPicksSummary summary;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "original_task_id"))
    private TaskId originalTaskId;

    @Column(name = "content_snapshot", nullable = false, length = 255)
    private String contentSnapshot;

    @Column(name = "completed_snapshot", nullable = false)
    private boolean completedSnapshot;

    @Column(name = "estimated_minutes_snapshot")
    private Integer estimatedMinutesSnapshot;

    @Column(name = "memo_snapshot", length = 500)
    private String memoSnapshot;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public static DailyTopPicksSummaryItem create(
            DailyTopPicksSummary summary,
            Task task,
            int displayOrder
    ) {
        if (!task.isTopPicked()) {
            throw new IllegalArgumentException("Top-picked task만 summary item으로 생성할 수 있습니다.");
        }

        TopPickDetail topPickDetail = task.getTopPickDetail();
        if (topPickDetail == null) {
            throw new IllegalStateException("Top-picked task에는 TopPickDetail이 존재해야 합니다.");
        }

        return DailyTopPicksSummaryItem.builder()
                .id(DailyTopPicksSummaryItemId.newId())
                .summary(summary)
                .originalTaskId(task.getTaskId())
                .contentSnapshot(task.getContent())
                .completedSnapshot(task.isCompleted())
                .estimatedMinutesSnapshot(topPickDetail.getEstimatedMinutes())
                .memoSnapshot(topPickDetail.getMemo())
                .displayOrder(displayOrder)
                .build();
    }

}

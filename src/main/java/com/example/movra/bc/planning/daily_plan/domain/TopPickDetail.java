package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.planning.daily_plan.domain.vo.TopPickDetailId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_top_pick_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TopPickDetail {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "top_pick_detail_id"))
    private TopPickDetailId topPickDetailId;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(length = 255, nullable = false)
    private String memo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    public static TopPickDetail create(int estimatedMinutes, String memo, Task task){
        return TopPickDetail.builder()
                .topPickDetailId(TopPickDetailId.newId())
                .estimatedMinutes(estimatedMinutes)
                .memo(memo)
                .task(task)
                .build();
    }

    void updateEstimatedMinutes(int newEstimatedMinutes) {
        this.estimatedMinutes = newEstimatedMinutes;
    }
}

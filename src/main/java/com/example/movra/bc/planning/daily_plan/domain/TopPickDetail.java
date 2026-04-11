package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTopPickEstimatedMinutesException;
import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTopPickMemoException;
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

    private static final int MAX_MEMO_LENGTH = 255;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "top_pick_detail_id"))
    private TopPickDetailId topPickDetailId;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(length = MAX_MEMO_LENGTH, nullable = false)
    private String memo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    public static TopPickDetail create(int estimatedMinutes, String memo, Task task){
        validateEstimatedMinutes(estimatedMinutes);
        validateMemo(memo);

        return TopPickDetail.builder()
                .topPickDetailId(TopPickDetailId.newId())
                .estimatedMinutes(estimatedMinutes)
                .memo(memo)
                .task(task)
                .build();
    }

    void updateEstimatedMinutes(int newEstimatedMinutes) {
        validateEstimatedMinutes(newEstimatedMinutes);
        this.estimatedMinutes = newEstimatedMinutes;
    }

    private static void validateEstimatedMinutes(int estimatedMinutes) {
        if (estimatedMinutes <= 0) {
            throw new InvalidTopPickEstimatedMinutesException();
        }
    }

    private static void validateMemo(String memo) {
        if (memo == null || memo.isBlank() || memo.length() > MAX_MEMO_LENGTH) {
            throw new InvalidTopPickMemoException();
        }
    }
}

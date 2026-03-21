package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.planning.daily_plan.domain.exception.TaskAlreadyCompletedException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TopPickDetailNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.type.TaskType;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Task {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "task_id"))
    private TaskId taskId;

    @Column(length = 255, nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean topPicked;

    @Column(nullable = false)
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskType taskType;

    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private TopPickDetail topPickDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    static Task createGeneral(String content, DailyPlan dailyPlan) {
        return create(content, TaskType.GENERAL, dailyPlan);
    }

    static Task createMorning(String content, DailyPlan dailyPlan) {
        return create(content, TaskType.MORNING, dailyPlan);
    }

    private static Task create(String content, TaskType taskType, DailyPlan dailyPlan) {
        return Task.builder()
                .taskId(TaskId.newId())
                .content(content)
                .topPicked(false)
                .completed(false)
                .taskType(taskType)
                .dailyPlan(dailyPlan)
                .build();
    }

    void update(String content) {
        this.content = content;
    }

    void markAsTopPicked(int estimatedMinutes, String memo) {
        validateNotCompleted();
        this.topPicked = true;
        this.topPickDetail = TopPickDetail.create(estimatedMinutes, memo, this);
    }

    void unmarkTopPicked() {
        validateNotCompleted();
        this.topPicked = false;
        this.topPickDetail = null;
    }

    void complete() {
        this.completed = true;
    }

    void unComplete() {
        this.completed = false;
    }

    void updateEstimatedMinutes(int newEstimatedMinutes) {
        if (this.topPickDetail == null) {
            throw new TopPickDetailNotFoundException();
        }
        this.topPickDetail.updateEstimatedMinutes(newEstimatedMinutes);
    }

    public boolean isGeneral() {
        return this.taskType == TaskType.GENERAL;
    }

    public boolean isMorning() {
        return this.taskType == TaskType.MORNING;
    }

    private void validateNotCompleted() {
        if (this.completed) {
            throw new TaskAlreadyCompletedException();
        }
    }
}

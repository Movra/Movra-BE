package com.example.movra.bc.planning.daily_plan.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.event.DailyPlanCreatedEvent;
import com.example.movra.bc.planning.daily_plan.domain.event.TaskTopPickUnpickedEvent;
import com.example.movra.bc.planning.daily_plan.domain.event.TaskTopPickedEvent;
import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTaskTypeException;
import com.example.movra.bc.planning.daily_plan.domain.exception.NotTopPickedTaskException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskAlreadyCompletedException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TopPickLimitExceededException;
import com.example.movra.bc.planning.daily_plan.domain.type.TaskType;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_plan", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "plan_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyPlan extends AbstractAggregateRoot {

    private static final int MAX_TOP_PICKS = 3;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_plan_id"))
    private DailyPlanId dailyPlanId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Builder.Default
    @OneToMany(mappedBy = "dailyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    public static DailyPlan create(UserId userId, LocalDate planDate) {
        DailyPlan dailyPlan = DailyPlan.builder()
                .dailyPlanId(DailyPlanId.newId())
                .userId(userId)
                .planDate(planDate)
                .build();

        dailyPlan.registerEvent(new DailyPlanCreatedEvent(dailyPlan.dailyPlanId));

        return dailyPlan;
    }

    public Task addTask(String content) {
        Task task = Task.createGeneral(content, this);
        this.tasks.add(task);
        return task;
    }

    public Task addMorningTask(String content){
        Task task = Task.createMorning(content, this);
        this.tasks.add(task);
        return task;
    }

    public void removeTask(TaskId taskId) {
        Task task = findModifiableTask(taskId);
        tasks.remove(task);
    }

    public void updateTask(TaskId taskId, String content) {
        Task task = findModifiableTask(taskId);
        task.update(content);
    }

    public void completeTask(TaskId taskId) {
        findTask(taskId).complete();
    }

    public void unCompleteTask(TaskId taskId) {
        findTask(taskId).unComplete();
    }

    public void markAsTopPicked(TaskId taskId, int estimatedMinutes, String memo) {
        Task task = findTask(taskId);

        if (task.isTopPicked()) {
            return;
        }

        long count = tasks.stream().filter(Task::isTopPicked).count();

        if (count >= MAX_TOP_PICKS) {
            throw new TopPickLimitExceededException();
        }

        task.markAsTopPicked(estimatedMinutes, memo);
        registerEvent(new TaskTopPickedEvent(this.dailyPlanId, taskId));
    }

    public void unmarkTopPicked(TaskId taskId) {
        Task task = findTask(taskId);

        if (!task.isTopPicked()) {
            return;
        }

        task.unmarkTopPicked();
        registerEvent(new TaskTopPickUnpickedEvent(this.dailyPlanId, taskId));
    }

    public void updateEstimatedMinutes(TaskId taskId, int newEstimatedMinutes) {
        Task task = findTask(taskId);

        if (!task.isTopPicked()) {
            throw new NotTopPickedTaskException();
        }

        task.updateEstimatedMinutes(newEstimatedMinutes);
    }

    public void validateTaskType(TaskId taskId, TaskType expectedType) {
        Task task = findTask(taskId);

        if (task.getTaskType() != expectedType) {
            throw new InvalidTaskTypeException();
        }
    }

    public List<Task> getGeneralTasks() {
        return tasks.stream()
                .filter(Task::isGeneral)
                .toList();
    }

    public List<Task> getMorningTasks() {
        return tasks.stream()
                .filter(Task::isMorning)
                .toList();
    }

    private Task findTask(TaskId taskId) {
        return tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .orElseThrow(TaskNotFoundException::new);
    }

    private Task findModifiableTask(TaskId taskId) {
        Task task = findTask(taskId);
        if (task.isCompleted()) {
            throw new TaskAlreadyCompletedException();
        }
        return task;
    }
}

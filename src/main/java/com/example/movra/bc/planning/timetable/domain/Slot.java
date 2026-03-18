package com.example.movra.bc.planning.timetable.domain;

import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.vo.SlotId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_slot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Slot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "slot_id"))
    private SlotId slotId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "task_id", nullable = false))
    private TaskId taskId;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean topPick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    static Slot createTopPick(TaskId taskId, LocalTime startTime, LocalTime endTime, Timetable timetable) {
        return Slot.builder()
                .slotId(SlotId.newId())
                .taskId(taskId)
                .startTime(startTime)
                .endTime(endTime)
                .topPick(true)
                .timetable(timetable)
                .build();
    }

    static Slot create(TaskId taskId, LocalTime startTime, LocalTime endTime, Timetable timetable) {
        return Slot.builder()
                .slotId(SlotId.newId())
                .taskId(taskId)
                .startTime(startTime)
                .endTime(endTime)
                .timetable(timetable)
                .topPick(false)
                .build();
    }

    void reschedule(LocalTime newStart, LocalTime newEnd) {
        this.startTime = newStart;
        this.endTime = newEnd;
    }

    void markAsTopPick() {
        this.topPick = true;
    }

    void unmarkTopPick() {
        this.topPick = false;
    }

    public boolean overlapsWith(LocalTime otherStart, LocalTime otherEnd) {
        return this.startTime.isBefore(otherEnd) && otherStart.isBefore(this.endTime);
    }
}

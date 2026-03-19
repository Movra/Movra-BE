package com.example.movra.bc.planning.timetable.domain;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.event.SlotRescheduledEvent;
import com.example.movra.bc.planning.timetable.domain.exception.InvalidTimeRangeException;
import com.example.movra.bc.planning.timetable.domain.exception.SlotNotFoundException;
import com.example.movra.bc.planning.timetable.domain.exception.TimeOverlapException;
import com.example.movra.bc.planning.timetable.domain.exception.TopPickSlotLimitExceededException;
import com.example.movra.bc.planning.timetable.domain.exception.TopPicksNotFullyAssignedException;
import com.example.movra.bc.planning.timetable.domain.vo.SlotId;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_timetable")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Timetable extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "timetable_id"))
    private TimetableId timetableId;

    @Column(nullable = false)
    private int topPickTotal;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "daily_plan_id", nullable = false, unique = true))
    private DailyPlanId dailyPlanId;

    @Builder.Default
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Slot> slots = new ArrayList<>();

    public static Timetable create(DailyPlanId dailyPlanId, int topPickTotal) {
        if (topPickTotal < 0) {
            throw new IllegalArgumentException("topPickTotal must be >= 0");
        }

        return Timetable.builder()
                .timetableId(TimetableId.newId())
                .dailyPlanId(dailyPlanId)
                .topPickTotal(topPickTotal)
                .build();
    }

    public void assignTopPick(TaskId taskId, LocalTime startTime, LocalTime endTime) {
        long assignedTopPicks = slots.stream().filter(Slot::isTopPick).count();
        if (topPickTotal > 0 && assignedTopPicks >= topPickTotal) {
            throw new TopPickSlotLimitExceededException();
        }

        validateTimeRange(startTime, endTime);
        validateNoOverlap(startTime, endTime, null);

        Slot slot = Slot.createTopPick(taskId, startTime, endTime, this);
        this.slots.add(slot);
    }

    public void assignTask(TaskId taskId, LocalTime startTime, LocalTime endTime) {
        validateTopPicksAssigned();
        validateTimeRange(startTime, endTime);
        validateNoOverlap(startTime, endTime, null);

        Slot slot = Slot.create(taskId, startTime, endTime, this);
        this.slots.add(slot);
    }

    public void reschedule(SlotId slotId, LocalTime newStart, LocalTime newEnd) {
        Slot slot = findSlot(slotId);
        validateTimeRange(newStart, newEnd);
        validateNoOverlap(newStart, newEnd, slotId);

        slot.reschedule(newStart, newEnd);

        if (slot.isTopPick()) {
            int newMinutes = (int) Duration.between(newStart, newEnd).toMinutes();

            registerEvent(new SlotRescheduledEvent(
                    this.dailyPlanId,
                    slot.getTaskId(),
                    newMinutes
            ));
        }
    }

    public void onTopPickSelected(TaskId taskId) {
        slots.stream()
                .filter(s -> s.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(Slot::markAsTopPick);

        this.topPickTotal++;
    }

    public void onTopPickDeselected(TaskId taskId) {
        slots.stream()
                .filter(s -> s.getTaskId().equals(taskId) && s.isTopPick())
                .findFirst()
                .ifPresent(Slot::unmarkTopPick);

        if (this.topPickTotal > 0) {
            this.topPickTotal--;
        }
    }

    public void removeSlot(SlotId slotId) {
        Slot slot = findSlot(slotId);
        this.slots.remove(slot);
    }

    private void validateTopPicksAssigned() {
        long assignedTopPicks = slots.stream()
                .filter(Slot::isTopPick)
                .count();

        if (assignedTopPicks < topPickTotal) {
            throw new TopPicksNotFullyAssignedException();
        }
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new InvalidTimeRangeException();
        }
    }

    private void validateNoOverlap(LocalTime start, LocalTime end, SlotId excludeSlotId) {
        boolean overlaps = slots.stream()
                .filter(s -> excludeSlotId == null || !s.getSlotId().equals(excludeSlotId))
                .anyMatch(s -> s.overlapsWith(start, end));

        if (overlaps) {
            throw new TimeOverlapException();
        }
    }

    private Slot findSlot(SlotId slotId) {
        return slots.stream()
                .filter(slot -> slot.getSlotId().equals(slotId))
                .findFirst()
                .orElseThrow(SlotNotFoundException::new);
    }
}

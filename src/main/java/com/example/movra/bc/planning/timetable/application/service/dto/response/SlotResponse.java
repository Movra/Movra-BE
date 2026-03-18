package com.example.movra.bc.planning.timetable.application.service.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.Slot;
import lombok.Builder;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record SlotResponse(
        UUID slotId,
        UUID taskId,
        String content,
        LocalTime startTime,
        LocalTime endTime,
        boolean topPick
) {

    public static SlotResponse from(Slot slot, Map<TaskId, String> taskContentMap) {
        return SlotResponse.builder()
                .slotId(slot.getSlotId().id())
                .taskId(slot.getTaskId().id())
                .content(taskContentMap.getOrDefault(slot.getTaskId(), ""))
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .topPick(slot.isTopPick())
                .build();
    }
}

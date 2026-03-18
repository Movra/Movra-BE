package com.example.movra.bc.planning.timetable.application.service.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record TimetableResponse(
        UUID timetableId,
        UUID dailyPlanId,
        int topPickTotal,
        List<SlotResponse> slots
) {

    public static TimetableResponse from(Timetable timetable, DailyPlan dailyPlan) {
        Map<TaskId, String> taskContentMap = dailyPlan.getTasks().stream()
                .collect(Collectors.toMap(task -> task.getTaskId(), task -> task.getContent()));

        return TimetableResponse.builder()
                .timetableId(timetable.getTimetableId().id())
                .dailyPlanId(timetable.getDailyPlanId().id())
                .topPickTotal(timetable.getTopPickTotal())
                .slots(timetable.getSlots().stream()
                        .map(slot -> SlotResponse.from(slot, taskContentMap))
                        .toList())
                .build();
    }
}

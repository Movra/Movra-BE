package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.Task;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TopPicksResponse(
        UUID taskId,
        String content,
        boolean completed,
        int estimatedMinutes,
        String memo
) {

    public static TopPicksResponse from(Task task) {
        return TopPicksResponse.builder()
                .taskId(task.getTaskId().id())
                .content(task.getContent())
                .completed(task.isCompleted())
                .estimatedMinutes(task.getTopPickDetail().getEstimatedMinutes())
                .memo(task.getTopPickDetail().getMemo())
                .build();
    }
}

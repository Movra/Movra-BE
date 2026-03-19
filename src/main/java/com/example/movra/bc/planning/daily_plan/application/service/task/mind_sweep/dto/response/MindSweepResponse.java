package com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.Task;
import lombok.Builder;

import java.util.UUID;

@Builder
public record MindSweepResponse(
        UUID taskId,
        String content,
        boolean completed
) {

    public static MindSweepResponse from(Task task) {
        return MindSweepResponse.builder()
                .taskId(task.getTaskId().id())
                .content(task.getContent())
                .completed(task.isCompleted())
                .build();
    }
}

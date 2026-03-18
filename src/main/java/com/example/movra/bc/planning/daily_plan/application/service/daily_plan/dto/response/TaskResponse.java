package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.Task;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskResponse(
        UUID taskId,
        String content,
        boolean completed,
        boolean coreSelected,
        TopPickDetailResponse coreDetail
) {

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId().id())
                .content(task.getContent())
                .completed(task.isCompleted())
                .coreSelected(task.isTopPicked())
                .coreDetail(task.getTopPickDetail() != null
                        ? TopPickDetailResponse.from(task.getTopPickDetail())
                        : null)
                .build();
    }
}

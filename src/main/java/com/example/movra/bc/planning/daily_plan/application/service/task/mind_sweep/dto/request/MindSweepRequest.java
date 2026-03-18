package com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MindSweepRequest(

        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 255, message = "content 는 255자 이내로 작성해주세요")
        String content
) {
}

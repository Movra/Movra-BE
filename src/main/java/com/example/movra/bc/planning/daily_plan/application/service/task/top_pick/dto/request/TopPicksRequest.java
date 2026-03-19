package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TopPicksRequest(
        @Positive(message = "estimatedMinutes는 양수여야 합니다.")
        int estimatedMinutes,

        @NotBlank(message = "memo는 필수입니다.")
        @Size(max = 255, message = "content 는 255자 이내로 작성해주세요")
        String memo
) {
}

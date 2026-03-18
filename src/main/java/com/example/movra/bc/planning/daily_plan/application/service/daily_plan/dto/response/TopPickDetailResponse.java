package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.TopPickDetail;
import lombok.Builder;

@Builder
public record TopPickDetailResponse(
        int estimatedMinutes,
        String memo
) {

    public static TopPickDetailResponse from(TopPickDetail topPickDetail) {
        return TopPickDetailResponse.builder()
                .estimatedMinutes(topPickDetail.getEstimatedMinutes())
                .memo(topPickDetail.getMemo())
                .build();
    }
}

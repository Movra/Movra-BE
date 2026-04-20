package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto;

public record DailyTopPicksSummaryItemView(
        String content,
        boolean completed,
        Integer estimatedMinutes,
        String memo,
        int displayOrder
) {
}

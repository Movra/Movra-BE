package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record DailyPlanId(
        UUID id
) {

    public static DailyPlanId newId(){
        return new DailyPlanId(UUID.randomUUID());
    }

    public static DailyPlanId of(UUID dailyPlanId){
        return new DailyPlanId(dailyPlanId);
    }
}

package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyPlanId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static DailyPlanId newId(){
        return new DailyPlanId(UUID.randomUUID());
    }

    public static DailyPlanId of(UUID dailyPlanId){
        return new DailyPlanId(dailyPlanId);
    }
}

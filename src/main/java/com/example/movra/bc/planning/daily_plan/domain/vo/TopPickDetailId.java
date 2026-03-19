package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TopPickDetailId(
        UUID id
) {

    public static TopPickDetailId newId(){
        return new TopPickDetailId(UUID.randomUUID());
    }

    public static TopPickDetailId of(UUID topPickDetailId){
        return new TopPickDetailId(topPickDetailId);
    }
}

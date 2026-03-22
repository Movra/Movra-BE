package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TopPickDetailId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static TopPickDetailId newId(){
        return new TopPickDetailId(UUID.randomUUID());
    }

    public static TopPickDetailId of(UUID topPickDetailId){
        return new TopPickDetailId(topPickDetailId);
    }
}

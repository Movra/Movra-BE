package com.example.morva.domain.goal.goal.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record SubGoalId(
        UUID subGoalId
) implements Serializable {

    public static SubGoalId newId(){
        return new SubGoalId(UUID.randomUUID());
    }

    public static SubGoalId of(UUID subGoalId){
        return new SubGoalId(subGoalId);
    }
}

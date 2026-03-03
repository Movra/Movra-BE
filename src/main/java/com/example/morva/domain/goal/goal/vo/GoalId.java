package com.example.morva.domain.goal.goal.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record GoalId(
        UUID goalId
) implements Serializable {

    public static GoalId newId(){
        return new GoalId(UUID.randomUUID());
    }

    public static GoalId of(UUID goalId){
        return new GoalId(goalId);
    }
}

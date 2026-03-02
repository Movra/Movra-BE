package com.example.morva.domain.goal.goal.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record GoalId(
        UUID goalId
) {

    public GoalId newId(){
        return new GoalId(UUID.randomUUID());
    }

    public GoalId of(UUID goalId){
        return new GoalId(goalId);
    }
}

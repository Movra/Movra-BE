package com.example.morva.domain.goal.goal.vo;

import java.util.UUID;

public record SubGoalId(
        UUID subGoalId
) {

    public SubGoalId newId(){
        return new SubGoalId(UUID.randomUUID());
    }

    public SubGoalId of(UUID subGoalId){
        return new SubGoalId(subGoalId);
    }
}

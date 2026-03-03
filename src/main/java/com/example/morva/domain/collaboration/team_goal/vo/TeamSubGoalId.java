package com.example.morva.domain.collaboration.team_goal.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamSubGoalId(
        UUID teamSubGoalId
) implements Serializable {

    public static TeamSubGoalId newId(){
        return new TeamSubGoalId(UUID.randomUUID());
    }

    public TeamSubGoalId of(UUID teamSubGoalId){
        return new TeamSubGoalId(teamSubGoalId);
    }
}

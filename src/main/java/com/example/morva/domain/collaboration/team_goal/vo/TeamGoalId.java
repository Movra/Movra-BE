package com.example.morva.domain.collaboration.team_goal.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamGoalId(
        UUID teamGoalId
) {

    public TeamGoalId newId(){
        return new TeamGoalId(UUID.randomUUID());
    }

    public TeamGoalId of(UUID teamGoalId){
        return new TeamGoalId(teamGoalId);
    }
}

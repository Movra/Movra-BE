package com.example.morva.domain.collaboration.team_goal.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamGoalId(
        UUID teamGoalId
) implements Serializable {

    public static TeamGoalId newId(){
        return new TeamGoalId(UUID.randomUUID());
    }

    public TeamGoalId of(UUID teamGoalId){
        return new TeamGoalId(teamGoalId);
    }
}

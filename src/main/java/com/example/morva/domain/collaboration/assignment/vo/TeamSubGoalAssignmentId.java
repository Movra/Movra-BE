package com.example.morva.domain.collaboration.assignment.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamSubGoalAssignmentId(
        UUID teamSubGoalAssignmentId
) implements Serializable {

    public static TeamSubGoalAssignmentId newId(){
        return new TeamSubGoalAssignmentId(UUID.randomUUID());
    }

    public static TeamSubGoalAssignmentId of(UUID teamSubGoalAssignmentId){
        return new TeamSubGoalAssignmentId(teamSubGoalAssignmentId);
    }
}

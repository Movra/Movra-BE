package com.example.morva.domain.collaboration.assignment.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamSubGoalAssignmentId(
        UUID teamSubGoalAssignmentId
) {

    public TeamSubGoalAssignmentId newId(){
        return new TeamSubGoalAssignmentId(UUID.randomUUID());
    }

    public TeamSubGoalAssignmentId of(UUID teamSubGoalAssignmentId){
        return new TeamSubGoalAssignmentId(teamSubGoalAssignmentId);
    }
}

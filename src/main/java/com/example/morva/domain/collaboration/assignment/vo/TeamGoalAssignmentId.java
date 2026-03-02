package com.example.morva.domain.collaboration.assignment.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamGoalAssignmentId(
        UUID teamGoalAssignmentId
) {

    public TeamGoalAssignmentId newId(){
        return new TeamGoalAssignmentId(UUID.randomUUID());
    }

    public TeamGoalAssignmentId of(UUID teamGoalAssignmentId){
        return new TeamGoalAssignmentId(teamGoalAssignmentId);
    }
}

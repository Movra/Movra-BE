package com.example.morva.domain.collaboration.assignment.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamGoalAssignmentId(
        UUID teamGoalAssignmentId
) implements Serializable {

    public static TeamGoalAssignmentId newId(){
        return new TeamGoalAssignmentId(UUID.randomUUID());
    }

    public static TeamGoalAssignmentId of(UUID teamGoalAssignmentId){
        return new TeamGoalAssignmentId(teamGoalAssignmentId);
    }
}

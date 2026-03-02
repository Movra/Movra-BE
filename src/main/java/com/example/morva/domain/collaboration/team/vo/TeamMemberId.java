package com.example.morva.domain.collaboration.team.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamMemberId(
        UUID teamMemberId
) {

    public TeamMemberId newId(){
        return new TeamMemberId(UUID.randomUUID());
    }

    public TeamMemberId of(UUID teamId){
        return new TeamMemberId(teamId);
    }
}

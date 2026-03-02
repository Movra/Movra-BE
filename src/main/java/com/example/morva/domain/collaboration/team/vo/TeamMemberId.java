package com.example.morva.domain.collaboration.team.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamMemberId(
        UUID teamMemberId
) {

    public static TeamMemberId newId(){
        return new TeamMemberId(UUID.randomUUID());
    }

    public static TeamMemberId of(UUID teamId){
        return new TeamMemberId(teamId);
    }
}

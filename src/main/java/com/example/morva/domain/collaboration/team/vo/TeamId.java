package com.example.morva.domain.collaboration.team.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamId(
        UUID teamId
) {

    public TeamId newId(){
        return new TeamId(UUID.randomUUID());
    }

    public TeamId of(UUID teamId){
        return new TeamId(teamId);
    }
}

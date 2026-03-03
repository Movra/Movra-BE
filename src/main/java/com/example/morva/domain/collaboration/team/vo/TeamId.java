package com.example.morva.domain.collaboration.team.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record TeamId(
        UUID teamId
) implements Serializable {

    public static TeamId newId(){
        return new TeamId(UUID.randomUUID());
    }

    public static TeamId of(UUID teamId){
        return new TeamId(teamId);
    }
}

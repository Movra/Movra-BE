package com.example.movra.bc.account.domain.user.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record UserId(
        UUID id
) implements Serializable {

    public static UserId newId(){
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID userId){
        return new UserId(userId);
    }
}

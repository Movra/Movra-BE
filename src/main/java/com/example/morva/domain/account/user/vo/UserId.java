package com.example.morva.domain.account.user.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record UserId(
        UUID userId
) {

    public UserId newId(){
        return new UserId(UUID.randomUUID());
    }

    public UserId of(UUID userId){
        return new UserId(userId);
    }
}

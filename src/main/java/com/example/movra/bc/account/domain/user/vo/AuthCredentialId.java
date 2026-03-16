package com.example.movra.bc.account.domain.user.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record AuthCredentialId(
        UUID id
)implements Serializable {

    public static AuthCredentialId newId() {
        return new AuthCredentialId(UUID.randomUUID());
    }

    public static AuthCredentialId of(UUID authCredentialId) {
        return new AuthCredentialId(authCredentialId);
    }
}
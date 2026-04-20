package com.example.movra.bc.account.user.infrastructure.user.device;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DeviceTokenId(
        UUID id
) implements Serializable {

    public static DeviceTokenId newId() {
        return new DeviceTokenId(UUID.randomUUID());
    }

    public static DeviceTokenId of(UUID deviceTokenId) {
        return new DeviceTokenId(deviceTokenId);
    }
}

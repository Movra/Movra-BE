package com.example.movra.bc.account.device_token.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DeviceTokenId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static DeviceTokenId newId() {
        return new DeviceTokenId(UUID.randomUUID());
    }

    public static DeviceTokenId of(UUID deviceTokenId) {
        return new DeviceTokenId(deviceTokenId);
    }
}

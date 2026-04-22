package com.example.movra.bc.personalization.behavior_profile.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record BehaviorProfileId(
        UUID id
) implements Serializable {

    public static BehaviorProfileId newId() {
        return new BehaviorProfileId(UUID.randomUUID());
    }

    public static BehaviorProfileId of(UUID id) {
        return new BehaviorProfileId(id);
    }
}

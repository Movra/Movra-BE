package com.example.movra.bc.personalization.behavior_profile.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ProfileAdjustmentSuggestionId(
        UUID id
) implements Serializable {

    public static ProfileAdjustmentSuggestionId newId() {
        return new ProfileAdjustmentSuggestionId(UUID.randomUUID());
    }

    public static ProfileAdjustmentSuggestionId of(UUID id) {
        return new ProfileAdjustmentSuggestionId(id);
    }
}

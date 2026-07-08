package com.example.movra.bc.personalization.behavior_profile.application.service.dto.response;

import com.example.movra.bc.personalization.behavior_profile.domain.ProfileAdjustmentSuggestion;
import com.example.movra.bc.personalization.behavior_profile.domain.type.AdjustmentSuggestionStatus;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ProfileAdjustmentTarget;

import java.time.Instant;
import java.util.UUID;

public record ProfileAdjustmentResponse(
        UUID id,
        ProfileAdjustmentTarget target,
        String declaredValue,
        String observedValue,
        Integer suggestedStartHour,
        Integer suggestedEndHour,
        String suggestedValue,
        String message,
        AdjustmentSuggestionStatus status,
        Instant createdAt
) {

    public static ProfileAdjustmentResponse from(ProfileAdjustmentSuggestion suggestion) {
        return new ProfileAdjustmentResponse(
                suggestion.getId().id(),
                suggestion.getTarget(),
                suggestion.getDeclaredValue(),
                suggestion.getObservedValue(),
                suggestion.getSuggestedStartHour(),
                suggestion.getSuggestedEndHour(),
                suggestion.getSuggestedValue(),
                suggestion.getMessage(),
                suggestion.getStatus(),
                suggestion.getCreatedAt()
        );
    }
}

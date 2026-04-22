package com.example.movra.bc.personalization.behavior_profile.application.service.dto.request;

import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBehaviorProfileRequest(
        @NotNull
        ExecutionDifficulty executionDifficulty,

        @NotNull
        SocialPreference socialPreference,

        @NotNull
        RecoveryStyle recoveryStyle,

        @NotNull
        @Min(0) @Max(23)
        Integer preferredFocusStartHour,

        @NotNull
        @Min(0) @Max(23)
        Integer preferredFocusEndHour,

        @NotNull
        CoachingMode coachingMode
) {
}

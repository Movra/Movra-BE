package com.example.movra.bc.personalization.behavior_profile.application.service.dto.request;

import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficultyLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.FocusWindow;
import com.example.movra.bc.personalization.behavior_profile.domain.type.PlanningDepth;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreferenceLevel;
import jakarta.validation.constraints.NotNull;

public record CreateBehaviorProfileRequest(
        @NotNull
        ExecutionDifficultyLevel executionDifficultyLevel,

        @NotNull
        SocialPreferenceLevel socialPreferenceLevel,

        @NotNull
        RecoveryStyle recoveryStyle,

        @NotNull
        FocusWindow preferredFocusWindow,

        @NotNull
        PlanningDepth planningDepth
) {
}

package com.example.movra.bc.personalization.behavior_profile.application.service.dto.response;

import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingTone;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficultyLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.FocusWindow;
import com.example.movra.bc.personalization.behavior_profile.domain.type.PlanningDepth;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ReflectionMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreferenceLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.StartMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record BehaviorProfileResponse(
        UUID behaviorProfileId,
        ExecutionDifficultyLevel executionDifficultyLevel,
        SocialPreferenceLevel socialPreferenceLevel,
        RecoveryStyle recoveryStyle,
        FocusWindow preferredFocusWindow,
        PlanningDepth planningDepth,
        StartMode defaultStartMode,
        SocialMode defaultSocialMode,
        ReflectionMode defaultReflectionMode,
        CoachingTone defaultCoachingTone
) {

    public static BehaviorProfileResponse from(BehaviorProfile behaviorProfile) {
        return BehaviorProfileResponse.builder()
                .behaviorProfileId(behaviorProfile.getId().id())
                .executionDifficultyLevel(behaviorProfile.getExecutionDifficultyLevel())
                .socialPreferenceLevel(behaviorProfile.getSocialPreferenceLevel())
                .recoveryStyle(behaviorProfile.getRecoveryStyle())
                .preferredFocusWindow(behaviorProfile.getPreferredFocusWindow())
                .planningDepth(behaviorProfile.getPlanningDepth())
                .defaultStartMode(behaviorProfile.defaultStartMode())
                .defaultSocialMode(behaviorProfile.defaultSocialMode())
                .defaultReflectionMode(behaviorProfile.defaultReflectionMode())
                .defaultCoachingTone(behaviorProfile.defaultCoachingTone())
                .build();
    }
}

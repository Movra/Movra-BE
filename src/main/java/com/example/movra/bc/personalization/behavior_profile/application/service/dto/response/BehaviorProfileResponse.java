package com.example.movra.bc.personalization.behavior_profile.application.service.dto.response;

import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import lombok.Builder;

import java.util.UUID;

@Builder
public record BehaviorProfileResponse(
        UUID behaviorProfileId,
        ExecutionDifficulty executionDifficulty,
        SocialPreference socialPreference,
        RecoveryStyle recoveryStyle,
        int preferredFocusStartHour,
        int preferredFocusEndHour,
        CoachingMode coachingMode
) {

    public static BehaviorProfileResponse from(BehaviorProfile behaviorProfile) {
        return BehaviorProfileResponse.builder()
                .behaviorProfileId(behaviorProfile.getId().id())
                .executionDifficulty(behaviorProfile.getExecutionDifficulty())
                .socialPreference(behaviorProfile.getSocialPreference())
                .recoveryStyle(behaviorProfile.getRecoveryStyle())
                .preferredFocusStartHour(behaviorProfile.getPreferredFocusStartHour())
                .preferredFocusEndHour(behaviorProfile.getPreferredFocusEndHour())
                .coachingMode(behaviorProfile.getCoachingMode())
                .build();
    }
}

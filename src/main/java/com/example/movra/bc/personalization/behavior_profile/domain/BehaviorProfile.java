package com.example.movra.bc.personalization.behavior_profile.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingTone;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficultyLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.FocusWindow;
import com.example.movra.bc.personalization.behavior_profile.domain.type.PlanningDepth;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ReflectionMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreferenceLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.StartMode;
import com.example.movra.bc.personalization.behavior_profile.domain.exception.InvalidBehaviorProfileException;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.BehaviorProfileId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_behavior_profile", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BehaviorProfile extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "behavior_profile_id"))
    private BehaviorProfileId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false, unique = true))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_difficulty_level", nullable = false)
    private ExecutionDifficultyLevel executionDifficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_preference_level", nullable = false)
    private SocialPreferenceLevel socialPreferenceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "recovery_style", nullable = false)
    private RecoveryStyle recoveryStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_focus_window", nullable = false)
    private FocusWindow preferredFocusWindow;

    @Enumerated(EnumType.STRING)
    @Column(name = "planning_depth", nullable = false)
    private PlanningDepth planningDepth;

    public static BehaviorProfile create(
            UserId userId,
            ExecutionDifficultyLevel executionDifficultyLevel,
            SocialPreferenceLevel socialPreferenceLevel,
            RecoveryStyle recoveryStyle,
            FocusWindow preferredFocusWindow,
            PlanningDepth planningDepth
    ) {
        validate(
                userId,
                executionDifficultyLevel,
                socialPreferenceLevel,
                recoveryStyle,
                preferredFocusWindow,
                planningDepth
        );

        return BehaviorProfile.builder()
                .id(BehaviorProfileId.newId())
                .userId(userId)
                .executionDifficultyLevel(executionDifficultyLevel)
                .socialPreferenceLevel(socialPreferenceLevel)
                .recoveryStyle(recoveryStyle)
                .preferredFocusWindow(preferredFocusWindow)
                .planningDepth(planningDepth)
                .build();
    }

    public void update(
            ExecutionDifficultyLevel executionDifficultyLevel,
            SocialPreferenceLevel socialPreferenceLevel,
            RecoveryStyle recoveryStyle,
            FocusWindow preferredFocusWindow,
            PlanningDepth planningDepth
    ) {
        validate(
                this.userId,
                executionDifficultyLevel,
                socialPreferenceLevel,
                recoveryStyle,
                preferredFocusWindow,
                planningDepth
        );

        this.executionDifficultyLevel = executionDifficultyLevel;
        this.socialPreferenceLevel = socialPreferenceLevel;
        this.recoveryStyle = recoveryStyle;
        this.preferredFocusWindow = preferredFocusWindow;
        this.planningDepth = planningDepth;
    }

    public StartMode defaultStartMode() {
        if (executionDifficultyLevel == ExecutionDifficultyLevel.HIGH || planningDepth == PlanningDepth.LIGHT) {
            return StartMode.BIG1_FIRST;
        }
        return StartMode.FULL_PLANNING;
    }

    public SocialMode defaultSocialMode() {
        return switch (socialPreferenceLevel) {
            case LOW -> SocialMode.MINIMAL;
            case MEDIUM -> SocialMode.OPTIONAL;
            case HIGH -> SocialMode.PROMINENT;
        };
    }

    public ReflectionMode defaultReflectionMode() {
        if (recoveryStyle == RecoveryStyle.NEED_RESET) {
            return ReflectionMode.SHORT;
        }
        return ReflectionMode.GUIDED;
    }

    public CoachingTone defaultCoachingTone() {
        if (executionDifficultyLevel == ExecutionDifficultyLevel.HIGH || recoveryStyle == RecoveryStyle.NEED_RESET) {
            return CoachingTone.GENTLE;
        }
        return CoachingTone.DIRECT;
    }

    private static void validate(
            UserId userId,
            ExecutionDifficultyLevel executionDifficultyLevel,
            SocialPreferenceLevel socialPreferenceLevel,
            RecoveryStyle recoveryStyle,
            FocusWindow preferredFocusWindow,
            PlanningDepth planningDepth
    ) {
        if (userId == null
                || executionDifficultyLevel == null
                || socialPreferenceLevel == null
                || recoveryStyle == null
                || preferredFocusWindow == null
                || planningDepth == null) {
            throw new InvalidBehaviorProfileException();
        }
    }
}

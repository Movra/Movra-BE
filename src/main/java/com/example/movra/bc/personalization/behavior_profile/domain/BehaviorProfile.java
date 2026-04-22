package com.example.movra.bc.personalization.behavior_profile.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.exception.InvalidBehaviorProfileException;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
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

    private static final int MIN_HOUR = 0;
    private static final int MAX_HOUR = 23;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "behavior_profile_id"))
    private BehaviorProfileId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_difficulty", nullable = false)
    private ExecutionDifficulty executionDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_preference", nullable = false)
    private SocialPreference socialPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "recovery_style", nullable = false)
    private RecoveryStyle recoveryStyle;

    @Column(name = "preferred_focus_start_hour", nullable = false)
    private int preferredFocusStartHour;

    @Column(name = "preferred_focus_end_hour", nullable = false)
    private int preferredFocusEndHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "coaching_mode", nullable = false)
    private CoachingMode coachingMode;

    public static BehaviorProfile create(
            UserId userId,
            ExecutionDifficulty executionDifficulty,
            SocialPreference socialPreference,
            RecoveryStyle recoveryStyle,
            int preferredFocusStartHour,
            int preferredFocusEndHour,
            CoachingMode coachingMode
    ) {
        validate(userId, executionDifficulty, socialPreference, recoveryStyle,
                preferredFocusStartHour, preferredFocusEndHour, coachingMode);

        return BehaviorProfile.builder()
                .id(BehaviorProfileId.newId())
                .userId(userId)
                .executionDifficulty(executionDifficulty)
                .socialPreference(socialPreference)
                .recoveryStyle(recoveryStyle)
                .preferredFocusStartHour(preferredFocusStartHour)
                .preferredFocusEndHour(preferredFocusEndHour)
                .coachingMode(coachingMode)
                .build();
    }

    public void update(
            ExecutionDifficulty executionDifficulty,
            SocialPreference socialPreference,
            RecoveryStyle recoveryStyle,
            int preferredFocusStartHour,
            int preferredFocusEndHour,
            CoachingMode coachingMode
    ) {
        validateFields(executionDifficulty, socialPreference, recoveryStyle,
                preferredFocusStartHour, preferredFocusEndHour, coachingMode);

        this.executionDifficulty = executionDifficulty;
        this.socialPreference = socialPreference;
        this.recoveryStyle = recoveryStyle;
        this.preferredFocusStartHour = preferredFocusStartHour;
        this.preferredFocusEndHour = preferredFocusEndHour;
        this.coachingMode = coachingMode;
    }

    private static void validate(
            UserId userId,
            ExecutionDifficulty executionDifficulty,
            SocialPreference socialPreference,
            RecoveryStyle recoveryStyle,
            int preferredFocusStartHour,
            int preferredFocusEndHour,
            CoachingMode coachingMode
    ) {
        if (userId == null) {
            throw new InvalidBehaviorProfileException();
        }

        validateFields(executionDifficulty, socialPreference, recoveryStyle,
                preferredFocusStartHour, preferredFocusEndHour, coachingMode);
    }

    private static void validateFields(
            ExecutionDifficulty executionDifficulty,
            SocialPreference socialPreference,
            RecoveryStyle recoveryStyle,
            int preferredFocusStartHour,
            int preferredFocusEndHour,
            CoachingMode coachingMode
    ) {
        if (executionDifficulty == null || socialPreference == null
                || recoveryStyle == null || coachingMode == null) {
            throw new InvalidBehaviorProfileException();
        }

        if (preferredFocusStartHour < MIN_HOUR || preferredFocusStartHour > MAX_HOUR
                || preferredFocusEndHour < MIN_HOUR || preferredFocusEndHour > MAX_HOUR) {
            throw new InvalidBehaviorProfileException();
        }
    }
}

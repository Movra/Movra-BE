package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.UpdateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.UpdateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.exception.InvalidBehaviorProfileException;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UpdateBehaviorProfileServiceTest {

    @InjectMocks
    private UpdateBehaviorProfileService updateBehaviorProfileService;

    @Mock
    private BehaviorProfileRepository behaviorProfileRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("update succeeds")
    void update_success() {
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.LOW,
                SocialPreference.LOW,
                RecoveryStyle.QUICK_RESTART,
                8,
                17,
                CoachingMode.GENTLE
        );
        UpdateBehaviorProfileRequest request = new UpdateBehaviorProfileRequest(
                ExecutionDifficulty.HIGH,
                SocialPreference.HIGH,
                RecoveryStyle.SLOW_REBUILDER,
                10,
                22,
                CoachingMode.STRICT
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfile));

        updateBehaviorProfileService.update(request);

        assertThat(behaviorProfile.getExecutionDifficulty()).isEqualTo(ExecutionDifficulty.HIGH);
        assertThat(behaviorProfile.getSocialPreference()).isEqualTo(SocialPreference.HIGH);
        assertThat(behaviorProfile.getRecoveryStyle()).isEqualTo(RecoveryStyle.SLOW_REBUILDER);
        assertThat(behaviorProfile.getPreferredFocusStartHour()).isEqualTo(10);
        assertThat(behaviorProfile.getPreferredFocusEndHour()).isEqualTo(22);
        assertThat(behaviorProfile.getCoachingMode()).isEqualTo(CoachingMode.STRICT);
        then(behaviorProfileRepository).should().save(behaviorProfile);
    }

    @Test
    @DisplayName("update throws when profile is missing")
    void update_notFound_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> updateBehaviorProfileService.update(
                new UpdateBehaviorProfileRequest(
                        ExecutionDifficulty.MEDIUM,
                        SocialPreference.MEDIUM,
                        RecoveryStyle.NEEDS_REFLECTION,
                        9,
                        18,
                        CoachingMode.NEUTRAL
                )
        )).isInstanceOf(BehaviorProfileNotFoundException.class);
    }

    @Test
    @DisplayName("update throws when field is null")
    void update_nullField_throwsException() {
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfile));

        assertThatThrownBy(() -> updateBehaviorProfileService.update(
                new UpdateBehaviorProfileRequest(
                        null,
                        SocialPreference.MEDIUM,
                        RecoveryStyle.NEEDS_REFLECTION,
                        9,
                        18,
                        CoachingMode.NEUTRAL
                )
        )).isInstanceOf(InvalidBehaviorProfileException.class);
    }

    @Test
    @DisplayName("update throws when preferred focus hour is out of range")
    void update_invalidHour_throwsException() {
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfile));

        assertThatThrownBy(() -> updateBehaviorProfileService.update(
                new UpdateBehaviorProfileRequest(
                        ExecutionDifficulty.MEDIUM,
                        SocialPreference.MEDIUM,
                        RecoveryStyle.NEEDS_REFLECTION,
                        9,
                        24,
                        CoachingMode.NEUTRAL
                )
        )).isInstanceOf(InvalidBehaviorProfileException.class);
    }

    @Test
    @DisplayName("update throws when preferred focus hour is null")
    void update_nullHour_throwsException() {
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfile));

        assertThatThrownBy(() -> updateBehaviorProfileService.update(
                new UpdateBehaviorProfileRequest(
                        ExecutionDifficulty.MEDIUM,
                        SocialPreference.MEDIUM,
                        RecoveryStyle.NEEDS_REFLECTION,
                        9,
                        null,
                        CoachingMode.NEUTRAL
                )
        )).isInstanceOf(InvalidBehaviorProfileException.class);
    }
}

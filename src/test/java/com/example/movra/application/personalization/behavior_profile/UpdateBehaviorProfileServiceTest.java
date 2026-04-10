package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.UpdateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.UpdateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.exception.InvalidBehaviorProfileException;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficultyLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.FocusWindow;
import com.example.movra.bc.personalization.behavior_profile.domain.type.PlanningDepth;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreferenceLevel;
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
import static org.mockito.BDDMockito.given;
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
                ExecutionDifficultyLevel.HIGH,
                SocialPreferenceLevel.LOW,
                RecoveryStyle.NEED_RESET,
                FocusWindow.EVENING,
                PlanningDepth.LIGHT
        );
        UpdateBehaviorProfileRequest request = new UpdateBehaviorProfileRequest(
                ExecutionDifficultyLevel.LOW,
                SocialPreferenceLevel.HIGH,
                RecoveryStyle.IMMEDIATE_RETRY,
                FocusWindow.MORNING,
                PlanningDepth.DEEP
        );
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.of(behaviorProfile));

        updateBehaviorProfileService.update(request);

        assertThat(behaviorProfile.getExecutionDifficultyLevel()).isEqualTo(ExecutionDifficultyLevel.LOW);
        assertThat(behaviorProfile.getSocialPreferenceLevel()).isEqualTo(SocialPreferenceLevel.HIGH);
        assertThat(behaviorProfile.getRecoveryStyle()).isEqualTo(RecoveryStyle.IMMEDIATE_RETRY);
        assertThat(behaviorProfile.getPreferredFocusWindow()).isEqualTo(FocusWindow.MORNING);
        assertThat(behaviorProfile.getPlanningDepth()).isEqualTo(PlanningDepth.DEEP);
    }

    @Test
    @DisplayName("update throws when profile is missing")
    void update_notFound_throwsException() {
        givenCurrentUser();
        UpdateBehaviorProfileRequest request = new UpdateBehaviorProfileRequest(
                ExecutionDifficultyLevel.MEDIUM,
                SocialPreferenceLevel.MEDIUM,
                RecoveryStyle.IMMEDIATE_RETRY,
                FocusWindow.AFTERNOON,
                PlanningDepth.BALANCED
        );
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> updateBehaviorProfileService.update(request))
                .isInstanceOf(BehaviorProfileNotFoundException.class);
    }

    @Test
    @DisplayName("update throws when profile input is invalid")
    void update_invalidInput_throwsException() {
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficultyLevel.HIGH,
                SocialPreferenceLevel.LOW,
                RecoveryStyle.NEED_RESET,
                FocusWindow.EVENING,
                PlanningDepth.LIGHT
        );
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.of(behaviorProfile));

        assertThatThrownBy(() -> updateBehaviorProfileService.update(
                new UpdateBehaviorProfileRequest(
                        ExecutionDifficultyLevel.LOW,
                        null,
                        RecoveryStyle.IMMEDIATE_RETRY,
                        FocusWindow.MORNING,
                        PlanningDepth.DEEP
                )
        )).isInstanceOf(InvalidBehaviorProfileException.class);
    }
}

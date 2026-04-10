package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileAlreadyExistsException;
import com.example.movra.bc.personalization.behavior_profile.application.service.CreateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.CreateBehaviorProfileRequest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateBehaviorProfileServiceTest {

    @InjectMocks
    private CreateBehaviorProfileService createBehaviorProfileService;

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
    @DisplayName("행동 프로필 생성 성공")
    void create_success() {
        // given
        givenCurrentUser();
        CreateBehaviorProfileRequest request = new CreateBehaviorProfileRequest(
                ExecutionDifficultyLevel.HIGH,
                SocialPreferenceLevel.LOW,
                RecoveryStyle.NEED_RESET,
                FocusWindow.EVENING,
                PlanningDepth.LIGHT
        );
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);

        // when
        createBehaviorProfileService.create(request);

        // then
        then(behaviorProfileRepository).should().save(any());
    }

    @Test
    @DisplayName("행동 프로필이 이미 존재하면 BehaviorProfileAlreadyExistsException 발생")
    void create_alreadyExists_throwsException() {
        // given
        givenCurrentUser();
        CreateBehaviorProfileRequest request = new CreateBehaviorProfileRequest(
                ExecutionDifficultyLevel.MEDIUM,
                SocialPreferenceLevel.MEDIUM,
                RecoveryStyle.IMMEDIATE_RETRY,
                FocusWindow.AFTERNOON,
                PlanningDepth.BALANCED
        );
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> createBehaviorProfileService.create(request))
                .isInstanceOf(BehaviorProfileAlreadyExistsException.class);
    }
}

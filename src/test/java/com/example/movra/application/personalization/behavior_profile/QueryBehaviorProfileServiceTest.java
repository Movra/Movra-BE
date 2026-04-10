package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.QueryBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.BehaviorProfileResponse;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingTone;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficultyLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.FocusWindow;
import com.example.movra.bc.personalization.behavior_profile.domain.type.PlanningDepth;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ReflectionMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreferenceLevel;
import com.example.movra.bc.personalization.behavior_profile.domain.type.StartMode;
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
class QueryBehaviorProfileServiceTest {

    @InjectMocks
    private QueryBehaviorProfileService queryBehaviorProfileService;

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
    @DisplayName("행동 프로필 조회 성공")
    void query_success() {
        // given
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

        // when
        BehaviorProfileResponse response = queryBehaviorProfileService.query();

        // then
        assertThat(response.defaultStartMode()).isEqualTo(StartMode.BIG1_FIRST);
        assertThat(response.defaultSocialMode()).isEqualTo(SocialMode.MINIMAL);
        assertThat(response.defaultReflectionMode()).isEqualTo(ReflectionMode.SHORT);
        assertThat(response.defaultCoachingTone()).isEqualTo(CoachingTone.GENTLE);
    }

    @Test
    @DisplayName("행동 프로필이 없으면 BehaviorProfileNotFoundException 발생")
    void query_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryBehaviorProfileService.query())
                .isInstanceOf(BehaviorProfileNotFoundException.class);
    }
}

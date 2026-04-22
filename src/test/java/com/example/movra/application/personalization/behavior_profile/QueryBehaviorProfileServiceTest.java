package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.QueryBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.BehaviorProfileResponse;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
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
    @DisplayName("내 행동 프로필 조회 성공")
    void queryMine_success() {
        // given
        givenCurrentUser();
        BehaviorProfile behaviorProfile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.MEDIUM,
                SocialPreference.HIGH,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfile));

        // when
        BehaviorProfileResponse response = queryBehaviorProfileService.queryMine();

        // then
        assertThat(response.executionDifficulty()).isEqualTo(ExecutionDifficulty.MEDIUM);
        assertThat(response.socialPreference()).isEqualTo(SocialPreference.HIGH);
        assertThat(response.recoveryStyle()).isEqualTo(RecoveryStyle.NEEDS_REFLECTION);
        assertThat(response.preferredFocusStartHour()).isEqualTo(9);
        assertThat(response.preferredFocusEndHour()).isEqualTo(18);
        assertThat(response.coachingMode()).isEqualTo(CoachingMode.NEUTRAL);
    }

    @Test
    @DisplayName("프로필이 없으면 BehaviorProfileNotFoundException 발생")
    void queryMine_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryBehaviorProfileService.queryMine())
                .isInstanceOf(BehaviorProfileNotFoundException.class);
    }
}

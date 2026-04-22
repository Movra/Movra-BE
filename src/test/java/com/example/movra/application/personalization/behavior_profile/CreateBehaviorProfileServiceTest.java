package com.example.movra.application.personalization.behavior_profile;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileAlreadyExistsException;
import com.example.movra.bc.personalization.behavior_profile.application.service.CreateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.CreateBehaviorProfileRequest;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;

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

    private DataIntegrityViolationException duplicateKeyViolation() {
        return new DataIntegrityViolationException(
                "duplicate",
                new SQLIntegrityConstraintViolationException("duplicate", "23000", 1062)
        );
    }

    private DataIntegrityViolationException otherIntegrityViolation() {
        return new DataIntegrityViolationException(
                "integrity",
                new SQLException("integrity", "23514", 23514)
        );
    }

    private CreateBehaviorProfileRequest validRequest() {
        return new CreateBehaviorProfileRequest(
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );
    }

    @Test
    @DisplayName("create succeeds")
    void create_success() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);

        createBehaviorProfileService.create(validRequest());

        then(behaviorProfileRepository).should().saveAndFlush(any());
    }

    @Test
    @DisplayName("create throws when profile already exists")
    void create_alreadyExists_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(true);

        assertThatThrownBy(() -> createBehaviorProfileService.create(validRequest()))
                .isInstanceOf(BehaviorProfileAlreadyExistsException.class);
    }

    @Test
    @DisplayName("create converts unique constraint violations to BehaviorProfileAlreadyExistsException")
    void create_duplicateAtWrite_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);
        given(behaviorProfileRepository.saveAndFlush(any()))
                .willThrow(duplicateKeyViolation());

        assertThatThrownBy(() -> createBehaviorProfileService.create(validRequest()))
                .isInstanceOf(BehaviorProfileAlreadyExistsException.class);
    }

    @Test
    @DisplayName("create rethrows non-duplicate integrity violations")
    void create_otherIntegrityViolation_rethrowsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);
        given(behaviorProfileRepository.saveAndFlush(any()))
                .willThrow(otherIntegrityViolation());

        assertThatThrownBy(() -> createBehaviorProfileService.create(validRequest()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("create throws when required field is null")
    void create_nullField_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);
        CreateBehaviorProfileRequest request = new CreateBehaviorProfileRequest(
                null,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                9,
                18,
                CoachingMode.NEUTRAL
        );

        assertThatThrownBy(() -> createBehaviorProfileService.create(request))
                .isInstanceOf(InvalidBehaviorProfileException.class);
    }

    @Test
    @DisplayName("create throws when preferred focus hour is out of range")
    void create_invalidHour_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);
        CreateBehaviorProfileRequest request = new CreateBehaviorProfileRequest(
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                -1,
                18,
                CoachingMode.NEUTRAL
        );

        assertThatThrownBy(() -> createBehaviorProfileService.create(request))
                .isInstanceOf(InvalidBehaviorProfileException.class);
    }

    @Test
    @DisplayName("create throws when preferred focus hour is null")
    void create_nullHour_throwsException() {
        givenCurrentUser();
        given(behaviorProfileRepository.existsByUserId(userId)).willReturn(false);
        CreateBehaviorProfileRequest request = new CreateBehaviorProfileRequest(
                ExecutionDifficulty.MEDIUM,
                SocialPreference.MEDIUM,
                RecoveryStyle.NEEDS_REFLECTION,
                null,
                18,
                CoachingMode.NEUTRAL
        );

        assertThatThrownBy(() -> createBehaviorProfileService.create(request))
                .isInstanceOf(InvalidBehaviorProfileException.class);
    }
}

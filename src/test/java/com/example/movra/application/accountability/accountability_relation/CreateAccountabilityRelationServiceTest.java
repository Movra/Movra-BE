package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.CreateAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CreateAccountabilityRelationServiceTest {

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();

    private CreateAccountabilityRelationService createAccountabilityRelationService;

    @BeforeEach
    void setUp() {
        createAccountabilityRelationService = new CreateAccountabilityRelationService(
                accountabilityRelationRepository,
                currentUserQuery,
                clock,
                analyticsEventRecorder
        );
        given(currentUserQuery.currentUser()).willReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("create succeeds when current user has no accountability relation")
    void create_success() {
        // given
        given(accountabilityRelationRepository.existsBySubjectUserId(userId)).willReturn(false);
        given(accountabilityRelationRepository.saveAndFlush(any(AccountabilityRelation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        createAccountabilityRelationService.create(request());

        // then
        then(accountabilityRelationRepository).should(times(1)).saveAndFlush(
                argThat(relation -> relation.getSubjectUserId().equals(userId)
                        && relation.getInviteCode() != null
                        && relation.getVisibilityPolicy().allows(MonitoringTarget.FOCUS_SESSION))
        );
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.ACCOUNTABILITY_INVITE_SENT),
                argThat(properties -> properties.get("source").equals("CREATE_RELATION"))
        );
    }

    @Test
    @DisplayName("create throws when current user already has accountability relation")
    void create_existingRelation_throwsException() {
        // given
        given(accountabilityRelationRepository.existsBySubjectUserId(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> createAccountabilityRelationService.create(request()))
                .isInstanceOf(AccountabilityRelationAlreadyExistsException.class);
        then(accountabilityRelationRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create converts duplicate key race into AccountabilityRelationAlreadyExistsException")
    void create_duplicateKeyRace_throwsException() {
        // given
        given(accountabilityRelationRepository.existsBySubjectUserId(userId)).willReturn(false);
        DataIntegrityViolationException duplicateKey = new DataIntegrityViolationException(
                "duplicate",
                new SQLException("Duplicate entry", "23000", 1062)
        );
        given(accountabilityRelationRepository.saveAndFlush(any(AccountabilityRelation.class)))
                .willThrow(duplicateKey);

        // when & then
        assertThatThrownBy(() -> createAccountabilityRelationService.create(request()))
                .isInstanceOf(AccountabilityRelationAlreadyExistsException.class);
    }

    private VisibilityPolicyRequest request() {
        return new VisibilityPolicyRequest(Set.of(MonitoringTarget.FOCUS_SESSION));
    }
}

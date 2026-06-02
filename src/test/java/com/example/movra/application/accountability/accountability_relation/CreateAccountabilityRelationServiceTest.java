package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.InviteCodeIssuer;
import com.example.movra.bc.accountability.accountability_relation.application.service.CreateAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

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
    private InviteCodeIssuer inviteCodeIssuer;

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
                inviteCodeIssuer,
                currentUserQuery,
                analyticsEventRecorder
        );
        given(currentUserQuery.currentUser()).willReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("create issues a unique invite code and records analytics when user has no relation")
    void create_success() {
        // given
        AccountabilityRelation relation = AccountabilityRelation.create(
                userId, new VisibilityPolicy(Set.of(MonitoringTarget.FOCUS_SESSION)), clock
        );
        given(accountabilityRelationRepository.existsBySubjectUserId(userId)).willReturn(false);
        given(inviteCodeIssuer.issueForNewRelation(eq(userId), any(VisibilityPolicy.class)))
                .willReturn(relation);

        // when
        createAccountabilityRelationService.create(request());

        // then
        then(inviteCodeIssuer).should(times(1)).issueForNewRelation(
                eq(userId),
                argThat(policy -> policy.allows(MonitoringTarget.FOCUS_SESSION))
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
        then(inviteCodeIssuer).should(never()).issueForNewRelation(any(), any());
    }

    @Test
    @DisplayName("create propagates AccountabilityRelationAlreadyExistsException raised by the issuer on a race")
    void create_duplicateKeyRace_throwsException() {
        // given
        given(accountabilityRelationRepository.existsBySubjectUserId(userId)).willReturn(false);
        given(inviteCodeIssuer.issueForNewRelation(eq(userId), any(VisibilityPolicy.class)))
                .willThrow(new AccountabilityRelationAlreadyExistsException());

        // when & then
        assertThatThrownBy(() -> createAccountabilityRelationService.create(request()))
                .isInstanceOf(AccountabilityRelationAlreadyExistsException.class);
    }

    private VisibilityPolicyRequest request() {
        return new VisibilityPolicyRequest(Set.of(MonitoringTarget.FOCUS_SESSION));
    }
}

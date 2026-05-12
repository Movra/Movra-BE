package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.JoinAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.JoinAccountabilityRelationRequest;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JoinAccountabilityRelationServiceTest {

    @InjectMocks
    private JoinAccountabilityRelationService joinAccountabilityRelationService;

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Mock
    private NotificationGateway notificationGateway;

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Instant now = Instant.parse("2026-04-29T01:00:00Z");

    private void givenCurrentUser(UserId userId) {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    private void givenClock() {
        lenient().when(clock.instant()).thenReturn(now);
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    @DisplayName("join succeeds and records accountability friend joined event")
    void join_success() {
        UserId subjectUserId = UserId.newId();
        UserId watcherUserId = UserId.newId();
        givenCurrentUser(watcherUserId);
        givenClock();
        AccountabilityRelation relation = AccountabilityRelation.create(
                subjectUserId,
                new VisibilityPolicy(Set.of(MonitoringTarget.FOCUS_SESSION)),
                clock
        );
        String inviteCode = relation.getInviteCode().code();
        given(accountabilityRelationRepository.findByInviteCode_Code(inviteCode))
                .willReturn(Optional.of(relation));

        joinAccountabilityRelationService.join(new JoinAccountabilityRelationRequest(inviteCode));

        assertThat(relation.getWatcherUserId()).isEqualTo(watcherUserId);
        then(analyticsEventRecorder).should().recordSafely(
                eq(watcherUserId),
                eq(AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED),
                argThat(properties ->
                        properties.get("relationId").equals(relation.getId().id().toString())
                                && properties.get("subjectUserId").equals(subjectUserId.id().toString())
                                && properties.get("watcherUserId").equals(watcherUserId.id().toString())
                )
        );
        then(notificationGateway).should().sendSafely(
                eq(subjectUserId),
                argThat(payload ->
                        payload.type().name().equals("ACCOUNTABILITY_MESSAGE")
                                && payload.data().get("relationId").equals(relation.getId().id().toString())
                                && payload.data().get("watcherUserId").equals(watcherUserId.id().toString())
                )
        );
    }

    @Test
    @DisplayName("join throws InvalidInviteCodeException when invite code does not exist")
    void join_invalidInviteCode_throwsException() {
        JoinAccountabilityRelationRequest request = new JoinAccountabilityRelationRequest("invalid-code");
        given(accountabilityRelationRepository.findByInviteCode_Code("invalid-code")).willReturn(Optional.empty());

        assertThatThrownBy(() -> joinAccountabilityRelationService.join(request))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}

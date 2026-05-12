package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.DisconnectAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.UpdateVisibilityPolicyService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryFriendAccountabilityStatusService;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FriendAccountabilityServiceTest {

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();
    private final UserId friendUserId = UserId.newId();

    private QueryFriendAccountabilityStatusService queryFriendAccountabilityStatusService;
    private UpdateVisibilityPolicyService updateVisibilityPolicyService;
    private DisconnectAccountabilityRelationService disconnectAccountabilityRelationService;

    @BeforeEach
    void setUp() {
        queryFriendAccountabilityStatusService = new QueryFriendAccountabilityStatusService(
                accountabilityRelationRepository,
                currentUserQuery
        );
        updateVisibilityPolicyService = new UpdateVisibilityPolicyService(
                accountabilityRelationRepository,
                currentUserQuery
        );
        disconnectAccountabilityRelationService = new DisconnectAccountabilityRelationService(
                accountabilityRelationRepository,
                currentUserQuery
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("query returns watched-by-friend and watching-friend lists")
    void query_returnsFriendStatus() {
        givenCurrentUser();
        AccountabilityRelation watchedByFriend = relation(userId, friendUserId);
        AccountabilityRelation watchingFriend = relation(friendUserId, userId);
        given(accountabilityRelationRepository.findAllBySubjectUserId(userId))
                .willReturn(List.of(watchedByFriend));
        given(accountabilityRelationRepository.findAllByWatcherUserId(userId))
                .willReturn(List.of(watchingFriend));

        FriendAccountabilityStatusResponse response = queryFriendAccountabilityStatusService.query();

        assertThat(response.watchedByFriends()).hasSize(1);
        assertThat(response.watchedByFriends().get(0).subjectUserId()).isEqualTo(userId.id());
        assertThat(response.watchedByFriends().get(0).watcherUserId()).isEqualTo(friendUserId.id());
        assertThat(response.watchingFriends()).hasSize(1);
        assertThat(response.watchingFriends().get(0).subjectUserId()).isEqualTo(friendUserId.id());
        assertThat(response.watchingFriends().get(0).watcherUserId()).isEqualTo(userId.id());
    }

    @Test
    @DisplayName("update visibility policy succeeds for subject user")
    void updateVisibilityPolicy_success() {
        givenCurrentUser();
        AccountabilityRelation relation = relation(userId, friendUserId);
        VisibilityPolicyRequest request = new VisibilityPolicyRequest(
                Set.of(MonitoringTarget.FOCUS_SESSION, MonitoringTarget.TIMETABLE_TASK)
        );
        given(accountabilityRelationRepository.findBySubjectUserId(userId))
                .willReturn(Optional.of(relation));
        given(accountabilityRelationRepository.save(relation)).willReturn(relation);

        FriendAccountabilityRelationResponse response = updateVisibilityPolicyService.update(request);

        assertThat(response.allowedTargets())
                .containsExactlyInAnyOrder(MonitoringTarget.FOCUS_SESSION, MonitoringTarget.TIMETABLE_TASK);
        then(accountabilityRelationRepository).should().save(relation);
    }

    @Test
    @DisplayName("update visibility policy throws when relation is missing")
    void updateVisibilityPolicy_missingRelation_throwsException() {
        givenCurrentUser();
        given(accountabilityRelationRepository.findBySubjectUserId(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> updateVisibilityPolicyService.update(
                new VisibilityPolicyRequest(Set.of(MonitoringTarget.FOCUS_SESSION))
        )).isInstanceOf(AccountabilityRelationNotFoundException.class);
    }

    @Test
    @DisplayName("removeWatcherFromMyRelation disconnects the watcher")
    void removeWatcherFromMyRelation_success() {
        givenCurrentUser();
        AccountabilityRelation relation = relation(userId, friendUserId);
        given(accountabilityRelationRepository.findBySubjectUserId(userId))
                .willReturn(Optional.of(relation));

        disconnectAccountabilityRelationService.removeWatcherFromMyRelation();

        assertThat(relation.getWatcherUserId()).isNull();
        assertThat(relation.getInviteCode()).isNull();
        then(accountabilityRelationRepository).should().save(relation);
    }

    @Test
    @DisplayName("stopWatchingFriend disconnects current user from watched friend")
    void stopWatchingFriend_success() {
        givenCurrentUser();
        AccountabilityRelation relation = relation(friendUserId, userId);
        given(accountabilityRelationRepository.findByWatcherUserId(userId))
                .willReturn(Optional.of(relation));

        disconnectAccountabilityRelationService.stopWatchingFriend();

        assertThat(relation.getWatcherUserId()).isNull();
        assertThat(relation.getInviteCode()).isNull();
        then(accountabilityRelationRepository).should().save(relation);
    }

    private AccountabilityRelation relation(UserId subjectUserId, UserId watcherUserId) {
        AccountabilityRelation relation = AccountabilityRelation.create(
                subjectUserId,
                new VisibilityPolicy(Set.of(MonitoringTarget.FOCUS_SESSION, MonitoringTarget.TOP_PICKS)),
                clock
        );
        relation.joinByInviteCode(relation.getInviteCode().code(), watcherUserId, clock);
        return relation;
    }
}

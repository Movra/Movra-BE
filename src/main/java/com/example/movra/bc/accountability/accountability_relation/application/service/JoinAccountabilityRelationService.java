package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.JoinAccountabilityRelationRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JoinAccountabilityRelationService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final NotificationGateway notificationGateway;

    @Transactional
    public FriendAccountabilityRelationResponse join(JoinAccountabilityRelationRequest request) {
        AccountabilityRelation accountabilityRelation =
                accountabilityRelationRepository.findByInviteCode_Code(request.inviteCode())
                        .orElseThrow(InvalidInviteCodeException::new);

        UserId watcherUserId = currentUserQuery.currentUser().userId();
        accountabilityRelation.joinByInviteCode(
                request.inviteCode(),
                watcherUserId,
                clock
        );
        analyticsEventRecorder.recordSafely(
                watcherUserId,
                AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED,
                Map.of(
                        "relationId", accountabilityRelation.getId().id().toString(),
                        "subjectUserId", accountabilityRelation.getSubjectUserId().id().toString(),
                        "watcherUserId", watcherUserId.id().toString()
                )
        );
        notificationGateway.sendSafely(
                accountabilityRelation.getSubjectUserId(),
                NotificationPayload.of(
                        NotificationType.ACCOUNTABILITY_MESSAGE,
                        "친구 연결 완료",
                        "새 감시 친구가 연결됐어요.",
                        Map.of(
                                "relationId", accountabilityRelation.getId().id().toString(),
                                "subjectUserId", accountabilityRelation.getSubjectUserId().id().toString(),
                                "watcherUserId", watcherUserId.id().toString()
                        )
                )
        );
        return FriendAccountabilityRelationResponse.from(accountabilityRelation);
    }
}

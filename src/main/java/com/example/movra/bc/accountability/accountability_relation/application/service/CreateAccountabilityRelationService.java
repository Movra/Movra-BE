package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.InviteCodeIssuer;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationAlreadyExistsException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateAccountabilityRelationService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final InviteCodeIssuer inviteCodeIssuer;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public void create(VisibilityPolicyRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (accountabilityRelationRepository.existsBySubjectUserId(userId)) {
            throw new AccountabilityRelationAlreadyExistsException();
        }

        AccountabilityRelation accountabilityRelation = inviteCodeIssuer.issueForNewRelation(
                userId, new VisibilityPolicy(request.targets())
        );

        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.ACCOUNTABILITY_INVITE_SENT,
                Map.of(
                        "relationId", accountabilityRelation.getId().id().toString(),
                        "source", "CREATE_RELATION"
                )
        );
    }
}

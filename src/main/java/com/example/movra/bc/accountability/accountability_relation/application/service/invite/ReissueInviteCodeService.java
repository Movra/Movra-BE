package com.example.movra.bc.accountability.accountability_relation.application.service.invite;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityInviteCode;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReissueInviteCodeService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public InviteCodeResponse reissue() {
        UserId userId = currentUserQuery.currentUser().userId();
        AccountabilityRelation accountabilityRelation = accountabilityRelationRepository.findBySubjectUserId(userId)
                .orElseThrow(AccountabilityRelationNotFoundException::new);
        AccountabilityInviteCode inviteCode = accountabilityRelation.generateInviteCode(userId, clock);

        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.ACCOUNTABILITY_INVITE_SENT,
                Map.of(
                        "relationId", accountabilityRelation.getId().id().toString(),
                        "source", "REISSUE_INVITE_CODE"
                )
        );
        return InviteCodeResponse.from(inviteCode);
    }
}

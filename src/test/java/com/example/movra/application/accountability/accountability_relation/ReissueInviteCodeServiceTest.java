package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.InviteCodeIssuer;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.ReissueInviteCodeService;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReissueInviteCodeServiceTest {

    @InjectMocks
    private ReissueInviteCodeService reissueInviteCodeService;

    @Mock
    private InviteCodeIssuer inviteCodeIssuer;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Test
    @DisplayName("reissue throws AccountabilityRelationNotFoundException when relation does not exist")
    void reissue_relationNotFound_throwsException() {
        UserId userId = UserId.newId();
        given(currentUserQuery.currentUser()).willReturn(AuthenticatedUser.builder().userId(userId).build());
        given(inviteCodeIssuer.reissueForSubject(userId)).willThrow(new AccountabilityRelationNotFoundException());

        assertThatThrownBy(() -> reissueInviteCodeService.reissue())
                .isInstanceOf(AccountabilityRelationNotFoundException.class);
    }
}

package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.ReissueInviteCodeService;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReissueInviteCodeServiceTest {

    @InjectMocks
    private ReissueInviteCodeService reissueInviteCodeService;

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("reissue throws AccountabilityRelationNotFoundException when relation does not exist")
    void reissue_relationNotFound_throwsException() {
        UserId userId = UserId.newId();
        given(currentUserQuery.currentUser()).willReturn(AuthenticatedUser.builder().userId(userId).build());
        given(accountabilityRelationRepository.findBySubjectUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reissueInviteCodeService.reissue())
                .isInstanceOf(AccountabilityRelationNotFoundException.class);
    }
}

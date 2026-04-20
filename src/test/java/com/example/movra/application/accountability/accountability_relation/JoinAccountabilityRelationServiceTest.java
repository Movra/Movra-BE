package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.accountability.accountability_relation.application.service.JoinAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.JoinAccountabilityRelationRequest;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
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
class JoinAccountabilityRelationServiceTest {

    @InjectMocks
    private JoinAccountabilityRelationService joinAccountabilityRelationService;

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("join throws InvalidInviteCodeException when invite code does not exist")
    void join_invalidInviteCode_throwsException() {
        JoinAccountabilityRelationRequest request = new JoinAccountabilityRelationRequest("invalid-code");
        given(accountabilityRelationRepository.findByInviteCode_Code("invalid-code")).willReturn(Optional.empty());

        assertThatThrownBy(() -> joinAccountabilityRelationService.join(request))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}

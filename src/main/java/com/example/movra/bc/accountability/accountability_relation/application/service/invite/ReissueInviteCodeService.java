package com.example.movra.bc.accountability.accountability_relation.application.service.invite;

import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class ReissueInviteCodeService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional
    public InviteCodeResponse reissue() {
        return InviteCodeResponse.from(
                accountabilityRelationRepository.findBySubjectUserId(currentUserQuery.currentUser().userId())
                        .orElseThrow(AccountabilityRelationNotFoundException::new)
                        .generateInviteCode(currentUserQuery.currentUser().userId(), clock)
        );
    }
}

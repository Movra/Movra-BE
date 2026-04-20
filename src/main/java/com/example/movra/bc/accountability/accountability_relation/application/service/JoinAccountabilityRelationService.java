package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.JoinAccountabilityRelationRequest;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class JoinAccountabilityRelationService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional
    public void join(JoinAccountabilityRelationRequest request) {
        AccountabilityRelation accountabilityRelation =
                accountabilityRelationRepository.findByInviteCode_Code(request.inviteCode())
                        .orElseThrow(InvalidInviteCodeException::new);

        accountabilityRelation.joinByInviteCode(request.inviteCode(), currentUserQuery.currentUser().userId(), clock);

        accountabilityRelationRepository.save(accountabilityRelation);
    }
}

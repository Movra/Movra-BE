package com.example.movra.bc.accountability.accountability_relation.application.service.invite;

import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class QueryInviteCodeStatusService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public InviteCodeStatusResponse query() {
        return InviteCodeStatusResponse.from(
                accountabilityRelationRepository.findBySubjectUserId(currentUserQuery.currentUser().userId())
                        .orElseThrow(AccountabilityRelationNotFoundException::new),
                clock
        );
    }
}

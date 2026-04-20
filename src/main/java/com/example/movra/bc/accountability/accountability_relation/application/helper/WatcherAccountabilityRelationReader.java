package com.example.movra.bc.accountability.accountability_relation.application.helper;

import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatcherAccountabilityRelationReader {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;

    public AccountabilityRelation getCurrentWatcherRelation() {
        return accountabilityRelationRepository.findByWatcherUserId(currentUserQuery.currentUser().userId())
                .orElseThrow(AccountabilityRelationNotFoundException::new);
    }
}

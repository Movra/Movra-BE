package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisconnectAccountabilityRelationService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void removeWatcherFromMyRelation() {
        UserId userId = currentUserQuery.currentUser().userId();
        AccountabilityRelation relation = accountabilityRelationRepository.findBySubjectUserId(userId)
                .orElseThrow(AccountabilityRelationNotFoundException::new);

        relation.disconnectWatcherBySubject(userId);
        accountabilityRelationRepository.save(relation);
    }

    @Transactional
    public void stopWatchingFriend() {
        UserId userId = currentUserQuery.currentUser().userId();
        AccountabilityRelation relation = accountabilityRelationRepository.findByWatcherUserId(userId)
                .orElseThrow(AccountabilityRelationNotFoundException::new);

        relation.disconnectWatcherByWatcher(userId);
        accountabilityRelationRepository.save(relation);
    }
}

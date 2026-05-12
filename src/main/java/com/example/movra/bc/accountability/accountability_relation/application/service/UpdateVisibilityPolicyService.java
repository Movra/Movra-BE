package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.AccountabilityRelationNotFoundException;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVisibilityPolicyService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public FriendAccountabilityRelationResponse update(VisibilityPolicyRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        AccountabilityRelation relation = accountabilityRelationRepository.findBySubjectUserId(userId)
                .orElseThrow(AccountabilityRelationNotFoundException::new);

        relation.updateVisibilityPolicy(userId, new VisibilityPolicy(request.targets()));

        return FriendAccountabilityRelationResponse.from(accountabilityRelationRepository.save(relation));
    }
}

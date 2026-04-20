package com.example.movra.bc.accountability.accountability_relation.application.service;

import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CreateAccountabilityRelationService {

    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional
    public void create(VisibilityPolicyRequest request){
        accountabilityRelationRepository.save(
                AccountabilityRelation.create(
                        currentUserQuery.currentUser().userId(),
                        new VisibilityPolicy(request.targets()),
                        clock
                )
        );
    }
}

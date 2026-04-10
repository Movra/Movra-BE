package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.BehaviorProfileResponse;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryBehaviorProfileService {

    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public BehaviorProfileResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();

        return behaviorProfileRepository.findByUserId(userId)
                .map(BehaviorProfileResponse::from)
                .orElseThrow(BehaviorProfileNotFoundException::new);
    }
}

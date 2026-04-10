package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.UpdateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBehaviorProfileService {

    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void update(UpdateBehaviorProfileRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        BehaviorProfile behaviorProfile = behaviorProfileRepository.findByUserId(userId)
                .orElseThrow(BehaviorProfileNotFoundException::new);

        behaviorProfile.update(
                request.executionDifficultyLevel(),
                request.socialPreferenceLevel(),
                request.recoveryStyle(),
                request.preferredFocusWindow(),
                request.planningDepth()
        );
    }
}

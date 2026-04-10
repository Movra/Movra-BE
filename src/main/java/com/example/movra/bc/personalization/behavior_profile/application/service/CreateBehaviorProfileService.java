package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileAlreadyExistsException;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.CreateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBehaviorProfileService {

    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(CreateBehaviorProfileRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (behaviorProfileRepository.existsByUserId(userId)) {
            throw new BehaviorProfileAlreadyExistsException();
        }

        behaviorProfileRepository.save(
                BehaviorProfile.create(
                        userId,
                        request.executionDifficultyLevel(),
                        request.socialPreferenceLevel(),
                        request.recoveryStyle(),
                        request.preferredFocusWindow(),
                        request.planningDepth()
                )
        );
    }
}

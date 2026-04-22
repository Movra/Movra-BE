package com.example.movra.bc.personalization.behavior_profile.application.service;

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
        BehaviorProfile behaviorProfile = behaviorProfileRepository.findByUserId(
                        currentUserQuery.currentUser().userId()
                )
                .orElseThrow(BehaviorProfileNotFoundException::new);

        behaviorProfile.update(
                request.executionDifficulty(),
                request.socialPreference(),
                request.recoveryStyle(),
                request.preferredFocusStartHour(),
                request.preferredFocusEndHour(),
                request.coachingMode()
        );

        behaviorProfileRepository.save(behaviorProfile);
    }
}

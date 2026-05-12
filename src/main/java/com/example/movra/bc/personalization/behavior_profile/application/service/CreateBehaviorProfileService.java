package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileAlreadyExistsException;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.CreateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateBehaviorProfileService {

    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public void create(CreateBehaviorProfileRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (behaviorProfileRepository.existsByUserId(userId)) {
            throw new BehaviorProfileAlreadyExistsException();
        }

        try {
            BehaviorProfile behaviorProfile = behaviorProfileRepository.saveAndFlush(
                    BehaviorProfile.create(
                            userId,
                            request.executionDifficulty(),
                            request.socialPreference(),
                            request.recoveryStyle(),
                            request.examTrack(),
                            request.preferredFocusStartHour(),
                            request.preferredFocusEndHour(),
                            request.coachingMode()
                    )
            );
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.BEHAVIOR_PROFILE_CREATED,
                    Map.of(
                            "behaviorProfileId", behaviorProfile.getId().id().toString(),
                            "examTrack", request.examTrack().name(),
                            "executionDifficulty", request.executionDifficulty().name()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                throw new BehaviorProfileAlreadyExistsException();
            }
            throw e;
        }
    }
}

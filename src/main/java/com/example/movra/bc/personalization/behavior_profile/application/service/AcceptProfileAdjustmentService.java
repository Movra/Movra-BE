package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.BehaviorProfileNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.application.exception.ProfileAdjustmentSuggestionNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.ProfileAdjustmentSuggestion;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.ProfileAdjustmentSuggestionRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.ProfileAdjustmentSuggestionId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 조정 제안을 수락해 BehaviorProfile에 실제로 반영한다.
 * 변경 대상 외의 필드는 현재 값을 그대로 유지한 채 update를 호출한다.
 */
@Service
@RequiredArgsConstructor
public class AcceptProfileAdjustmentService {

    private final ProfileAdjustmentSuggestionRepository profileAdjustmentSuggestionRepository;
    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void accept(UUID suggestionId) {
        UserId userId = currentUserQuery.currentUser().userId();

        ProfileAdjustmentSuggestion suggestion = profileAdjustmentSuggestionRepository
                .findByIdAndUserId(ProfileAdjustmentSuggestionId.of(suggestionId), userId)
                .orElseThrow(ProfileAdjustmentSuggestionNotFoundException::new);

        BehaviorProfile profile = behaviorProfileRepository.findByUserId(userId)
                .orElseThrow(BehaviorProfileNotFoundException::new);

        applyToProfile(profile, suggestion);
        suggestion.accept();
    }

    private void applyToProfile(BehaviorProfile profile, ProfileAdjustmentSuggestion suggestion) {
        switch (suggestion.getTarget()) {
            case FOCUS_HOURS -> profile.update(
                    profile.getExecutionDifficulty(),
                    profile.getSocialPreference(),
                    profile.getRecoveryStyle(),
                    profile.getExamTrack(),
                    suggestion.getSuggestedStartHour(),
                    suggestion.getSuggestedEndHour(),
                    profile.getCoachingMode()
            );
            case EXECUTION_DIFFICULTY -> profile.update(
                    ExecutionDifficulty.valueOf(suggestion.getSuggestedValue()),
                    profile.getSocialPreference(),
                    profile.getRecoveryStyle(),
                    profile.getExamTrack(),
                    profile.getPreferredFocusStartHour(),
                    profile.getPreferredFocusEndHour(),
                    profile.getCoachingMode()
            );
            case RECOVERY_STYLE -> profile.update(
                    profile.getExecutionDifficulty(),
                    profile.getSocialPreference(),
                    RecoveryStyle.valueOf(suggestion.getSuggestedValue()),
                    profile.getExamTrack(),
                    profile.getPreferredFocusStartHour(),
                    profile.getPreferredFocusEndHour(),
                    profile.getCoachingMode()
            );
        }
    }
}

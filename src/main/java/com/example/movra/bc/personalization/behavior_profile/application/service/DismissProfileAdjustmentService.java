package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.exception.ProfileAdjustmentSuggestionNotFoundException;
import com.example.movra.bc.personalization.behavior_profile.domain.ProfileAdjustmentSuggestion;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.ProfileAdjustmentSuggestionRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.ProfileAdjustmentSuggestionId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DismissProfileAdjustmentService {

    private final ProfileAdjustmentSuggestionRepository profileAdjustmentSuggestionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void dismiss(UUID suggestionId) {
        UserId userId = currentUserQuery.currentUser().userId();
        ProfileAdjustmentSuggestion suggestion = profileAdjustmentSuggestionRepository
                .findByIdAndUserId(ProfileAdjustmentSuggestionId.of(suggestionId), userId)
                .orElseThrow(ProfileAdjustmentSuggestionNotFoundException::new);
        suggestion.dismiss();
    }
}

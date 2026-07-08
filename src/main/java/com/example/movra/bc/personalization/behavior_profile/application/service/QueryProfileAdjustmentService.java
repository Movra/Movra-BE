package com.example.movra.bc.personalization.behavior_profile.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.ProfileAdjustmentResponse;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.ProfileAdjustmentSuggestionRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.AdjustmentSuggestionStatus;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryProfileAdjustmentService {

    private final ProfileAdjustmentSuggestionRepository profileAdjustmentSuggestionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<ProfileAdjustmentResponse> queryPending() {
        UserId userId = currentUserQuery.currentUser().userId();
        return profileAdjustmentSuggestionRepository
                .findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, AdjustmentSuggestionStatus.PENDING)
                .stream()
                .map(ProfileAdjustmentResponse::from)
                .toList();
    }
}

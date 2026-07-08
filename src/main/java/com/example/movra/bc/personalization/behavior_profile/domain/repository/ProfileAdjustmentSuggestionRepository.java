package com.example.movra.bc.personalization.behavior_profile.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.ProfileAdjustmentSuggestion;
import com.example.movra.bc.personalization.behavior_profile.domain.type.AdjustmentSuggestionStatus;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ProfileAdjustmentTarget;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.ProfileAdjustmentSuggestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileAdjustmentSuggestionRepository
        extends JpaRepository<ProfileAdjustmentSuggestion, ProfileAdjustmentSuggestionId> {

    Optional<ProfileAdjustmentSuggestion> findByIdAndUserId(ProfileAdjustmentSuggestionId id, UserId userId);

    List<ProfileAdjustmentSuggestion> findAllByUserIdAndStatusOrderByCreatedAtDesc(
            UserId userId, AdjustmentSuggestionStatus status);

    List<ProfileAdjustmentSuggestion> findAllByUserIdAndTargetAndStatus(
            UserId userId, ProfileAdjustmentTarget target, AdjustmentSuggestionStatus status);
}

package com.example.movra.bc.personalization.behavior_profile.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.type.AdjustmentSuggestionStatus;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ProfileAdjustmentTarget;
import com.example.movra.bc.personalization.behavior_profile.domain.vo.ProfileAdjustmentSuggestionId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * insight가 감지한 괴리에 대한 프로필 조정 제안. 자동 반영하지 않고 PENDING으로 쌓아두며,
 * 사용자가 수락(accept)할 때에만 BehaviorProfile에 반영한다.
 * - FOCUS_HOURS: suggestedStartHour/suggestedEndHour 사용.
 * - EXECUTION_DIFFICULTY / RECOVERY_STYLE: suggestedValue(enum name) 사용.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_profile_adjustment_suggestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileAdjustmentSuggestion extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "profile_adjustment_suggestion_id"))
    private ProfileAdjustmentSuggestionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false, length = 32)
    private ProfileAdjustmentTarget target;

    @Column(name = "declared_value", length = 64)
    private String declaredValue;

    @Column(name = "observed_value", length = 64)
    private String observedValue;

    @Column(name = "suggested_start_hour")
    private Integer suggestedStartHour;

    @Column(name = "suggested_end_hour")
    private Integer suggestedEndHour;

    @Column(name = "suggested_value", length = 64)
    private String suggestedValue;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AdjustmentSuggestionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static ProfileAdjustmentSuggestion create(
            UserId userId,
            ProfileAdjustmentTarget target,
            String declaredValue,
            String observedValue,
            Integer suggestedStartHour,
            Integer suggestedEndHour,
            String suggestedValue,
            String message,
            Instant createdAt
    ) {
        return ProfileAdjustmentSuggestion.builder()
                .id(ProfileAdjustmentSuggestionId.newId())
                .userId(userId)
                .target(target)
                .declaredValue(declaredValue)
                .observedValue(observedValue)
                .suggestedStartHour(suggestedStartHour)
                .suggestedEndHour(suggestedEndHour)
                .suggestedValue(suggestedValue)
                .message(message)
                .status(AdjustmentSuggestionStatus.PENDING)
                .createdAt(createdAt)
                .build();
    }

    public void accept() {
        this.status = AdjustmentSuggestionStatus.ACCEPTED;
    }

    public void dismiss() {
        this.status = AdjustmentSuggestionStatus.DISMISSED;
    }

    public boolean isPending() {
        return this.status == AdjustmentSuggestionStatus.PENDING;
    }
}

package com.example.movra.bc.analytics.activation_funnel.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_funnel.domain.vo.ActivationFunnelId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "tbl_activation_funnel",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activation_funnel_user_id",
                columnNames = "user_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActivationFunnel extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "activation_funnel_id"))
    private ActivationFunnelId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "signup_at")
    private Instant signupAt;

    @Column(name = "onboarding_started_at")
    private Instant onboardingStartedAt;

    @Column(name = "onboarding_completed_at")
    private Instant onboardingCompletedAt;

    @Column(name = "onboarding_skipped_at")
    private Instant onboardingSkippedAt;

    @Column(name = "first_session_started_at")
    private Instant firstSessionStartedAt;

    @Column(name = "first_session_completed_at")
    private Instant firstSessionCompletedAt;

    @Column(name = "nsm_first_entry_at")
    private Instant nsmFirstEntryAt;

    @Column(name = "activation_source", length = 64)
    private String activationSource;

    @Column(name = "grade_level", length = 64)
    private String gradeLevel;

    public static ActivationFunnel create(UserId userId) {
        return ActivationFunnel.builder()
                .id(ActivationFunnelId.newId())
                .userId(userId)
                .build();
    }

    public void markSignup(Instant occurredAt, String activationSource, String gradeLevel) {
        if (signupAt == null) {
            signupAt = occurredAt;
        }
        markActivationSource(activationSource);
        markGradeLevel(gradeLevel);
    }

    public void markOnboardingStarted(Instant occurredAt) {
        if (onboardingStartedAt == null) {
            onboardingStartedAt = occurredAt;
        }
    }

    public void markOnboardingCompleted(Instant occurredAt, String gradeLevel) {
        if (onboardingCompletedAt == null) {
            onboardingCompletedAt = occurredAt;
        }
        markGradeLevel(gradeLevel);
    }

    public void markOnboardingSkipped(Instant occurredAt) {
        if (onboardingSkippedAt == null) {
            onboardingSkippedAt = occurredAt;
        }
    }

    public void markFirstSessionStarted(Instant occurredAt) {
        if (firstSessionStartedAt == null) {
            firstSessionStartedAt = occurredAt;
        }
    }

    public void markFirstSessionCompleted(Instant occurredAt) {
        if (firstSessionCompletedAt == null) {
            firstSessionCompletedAt = occurredAt;
        }
        if (nsmFirstEntryAt == null) {
            nsmFirstEntryAt = occurredAt;
        }
    }

    private void markActivationSource(String activationSource) {
        String normalized = normalize(activationSource);
        if (this.activationSource == null && normalized != null) {
            this.activationSource = normalized;
        }
    }

    private void markGradeLevel(String gradeLevel) {
        String normalized = normalize(gradeLevel);
        if (this.gradeLevel == null && normalized != null) {
            this.gradeLevel = normalized;
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}

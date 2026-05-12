package com.example.movra.bc.analytics.activation_funnel.application.service;

import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_funnel.domain.ActivationFunnel;
import com.example.movra.bc.analytics.activation_funnel.domain.repository.ActivationFunnelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActivationFunnelProjector {

    private final ActivationFunnelRepository activationFunnelRepository;

    public void project(AnalyticsEvent event) {
        if (!supports(event.getEventType())) {
            return;
        }

        ActivationFunnel activationFunnel = activationFunnelRepository.findByUserId(event.getUserId())
                .orElseGet(() -> ActivationFunnel.create(event.getUserId()));

        apply(activationFunnel, event);
        activationFunnelRepository.save(activationFunnel);
    }

    private void apply(ActivationFunnel activationFunnel, AnalyticsEvent event) {
        Map<String, String> properties = event.getProperties();
        AnalyticsEventType eventType = event.getEventType();

        switch (eventType) {
            case SIGNUP -> activationFunnel.markSignup(
                    event.getOccurredAt(),
                    properties.get("source"),
                    properties.get("gradeLevel")
            );
            case ONBOARDING_STARTED -> activationFunnel.markOnboardingStarted(event.getOccurredAt());
            case BEHAVIOR_PROFILE_CREATED -> activationFunnel.markOnboardingCompleted(
                    event.getOccurredAt(),
                    properties.get("gradeLevel")
            );
            case ONBOARDING_SKIPPED -> activationFunnel.markOnboardingSkipped(event.getOccurredAt());
            case FOCUS_SESSION_STARTED -> activationFunnel.markFirstSessionStarted(event.getOccurredAt());
            case FOCUS_SESSION_COMPLETED -> activationFunnel.markFirstSessionCompleted(event.getOccurredAt());
            default -> {
            }
        }
    }

    private boolean supports(AnalyticsEventType eventType) {
        return switch (eventType) {
            case SIGNUP,
                 ONBOARDING_STARTED,
                 BEHAVIOR_PROFILE_CREATED,
                 ONBOARDING_SKIPPED,
                 FOCUS_SESSION_STARTED,
                 FOCUS_SESSION_COMPLETED -> true;
            default -> false;
        };
    }
}

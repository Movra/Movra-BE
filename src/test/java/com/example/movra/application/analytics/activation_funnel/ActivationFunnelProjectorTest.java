package com.example.movra.application.analytics.activation_funnel;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_funnel.application.service.ActivationFunnelProjector;
import com.example.movra.bc.analytics.activation_funnel.domain.ActivationFunnel;
import com.example.movra.bc.analytics.activation_funnel.domain.repository.ActivationFunnelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ActivationFunnelProjectorTest {

    @Mock
    private ActivationFunnelRepository activationFunnelRepository;

    private final UserId userId = UserId.newId();

    @Test
    @DisplayName("project accumulates activation funnel timestamps per user")
    void project_lifecycleEvents_updatesFunnel() {
        AtomicReference<ActivationFunnel> stored = givenInMemoryFunnelRepository();
        ActivationFunnelProjector projector = new ActivationFunnelProjector(activationFunnelRepository);

        projector.project(event(AnalyticsEventType.SIGNUP, "2026-04-29T00:00:00Z", Map.of("source", "LOCAL")));
        projector.project(event(AnalyticsEventType.BEHAVIOR_PROFILE_CREATED, "2026-04-29T00:02:00Z", Map.of()));
        projector.project(event(AnalyticsEventType.FOCUS_SESSION_STARTED, "2026-04-29T00:03:00Z", Map.of()));
        projector.project(event(AnalyticsEventType.FOCUS_SESSION_COMPLETED, "2026-04-29T00:06:00Z", Map.of()));

        ActivationFunnel activationFunnel = stored.get();
        assertThat(activationFunnel.getUserId()).isEqualTo(userId);
        assertThat(activationFunnel.getSignupAt()).isEqualTo(Instant.parse("2026-04-29T00:00:00Z"));
        assertThat(activationFunnel.getOnboardingCompletedAt()).isEqualTo(Instant.parse("2026-04-29T00:02:00Z"));
        assertThat(activationFunnel.getFirstSessionStartedAt()).isEqualTo(Instant.parse("2026-04-29T00:03:00Z"));
        assertThat(activationFunnel.getFirstSessionCompletedAt()).isEqualTo(Instant.parse("2026-04-29T00:06:00Z"));
        assertThat(activationFunnel.getNsmFirstEntryAt()).isEqualTo(Instant.parse("2026-04-29T00:06:00Z"));
        assertThat(activationFunnel.getActivationSource()).isEqualTo("LOCAL");
    }

    @Test
    @DisplayName("project does not treat abandoned focus sessions as NSM entry")
    void project_abandonedFocusSession_doesNotCompleteFunnel() {
        AtomicReference<ActivationFunnel> stored = givenInMemoryFunnelRepository();
        ActivationFunnelProjector projector = new ActivationFunnelProjector(activationFunnelRepository);

        projector.project(event(AnalyticsEventType.FOCUS_SESSION_STARTED, "2026-04-29T00:03:00Z", Map.of()));
        projector.project(event(AnalyticsEventType.FOCUS_SESSION_ABANDONED, "2026-04-29T00:04:00Z", Map.of()));

        ActivationFunnel activationFunnel = stored.get();
        assertThat(activationFunnel.getFirstSessionStartedAt()).isEqualTo(Instant.parse("2026-04-29T00:03:00Z"));
        assertThat(activationFunnel.getFirstSessionCompletedAt()).isNull();
        assertThat(activationFunnel.getNsmFirstEntryAt()).isNull();
    }

    private AtomicReference<ActivationFunnel> givenInMemoryFunnelRepository() {
        AtomicReference<ActivationFunnel> stored = new AtomicReference<>();
        given(activationFunnelRepository.findByUserId(userId)).willAnswer(invocation -> Optional.ofNullable(stored.get()));
        given(activationFunnelRepository.save(any(ActivationFunnel.class)))
                .willAnswer(invocation -> {
                    ActivationFunnel activationFunnel = invocation.getArgument(0, ActivationFunnel.class);
                    stored.set(activationFunnel);
                    return activationFunnel;
                });
        return stored;
    }

    private AnalyticsEvent event(AnalyticsEventType eventType, String occurredAt, Map<String, String> properties) {
        return AnalyticsEvent.record(
                userId,
                eventType,
                Instant.parse(occurredAt),
                properties
        );
    }
}

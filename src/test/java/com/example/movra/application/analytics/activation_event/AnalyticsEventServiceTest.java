package com.example.movra.application.analytics.activation_event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.application.service.QueryAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.RecordAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.dto.request.AnalyticsEventRequest;
import com.example.movra.bc.analytics.activation_event.application.service.dto.response.AnalyticsEventResponse;
import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.exception.InvalidAnalyticsEventException;
import com.example.movra.bc.analytics.activation_event.domain.repository.AnalyticsEventRepository;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();

    private RecordAnalyticsEventService recordAnalyticsEventService;
    private QueryAnalyticsEventService queryAnalyticsEventService;

    @BeforeEach
    void setUp() {
        AnalyticsEventRecorder analyticsEventRecorder = new AnalyticsEventRecorder(
                analyticsEventRepository,
                clock
        );
        recordAnalyticsEventService = new RecordAnalyticsEventService(
                analyticsEventRecorder,
                currentUserQuery
        );
        queryAnalyticsEventService = new QueryAnalyticsEventService(
                analyticsEventRepository,
                currentUserQuery,
                clock
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("record stores analytics event for current user")
    void record_success() {
        givenCurrentUser();
        given(analyticsEventRepository.save(org.mockito.ArgumentMatchers.any(AnalyticsEvent.class)))
                .willAnswer(invocation -> invocation.getArgument(0, AnalyticsEvent.class));
        AnalyticsEventRequest request = new AnalyticsEventRequest(
                AnalyticsEventType.FOCUS_SESSION_STARTED,
                Map.of("entryPoint", "home")
        );

        AnalyticsEventResponse response = recordAnalyticsEventService.record(request);

        assertThat(response.eventType()).isEqualTo(AnalyticsEventType.FOCUS_SESSION_STARTED);
        assertThat(response.occurredAt()).isEqualTo(clock.instant());
        assertThat(response.properties()).containsEntry("entryPoint", "home");
    }

    @Test
    @DisplayName("record normalizes null properties to empty properties")
    void record_nullProperties_returnsEmptyProperties() {
        givenCurrentUser();
        given(analyticsEventRepository.save(org.mockito.ArgumentMatchers.any(AnalyticsEvent.class)))
                .willAnswer(invocation -> invocation.getArgument(0, AnalyticsEvent.class));
        AnalyticsEventRequest request = new AnalyticsEventRequest(
                AnalyticsEventType.RECOVERY_CARD_VIEWED,
                null
        );

        AnalyticsEventResponse response = recordAnalyticsEventService.record(request);

        assertThat(response.properties()).isEmpty();
    }

    @Test
    @DisplayName("record rejects invalid properties")
    void record_invalidProperties_throwsException() {
        givenCurrentUser();
        AnalyticsEventRequest request = new AnalyticsEventRequest(
                AnalyticsEventType.TOP_PICK_SELECTED,
                Map.of("", "value")
        );

        assertThatThrownBy(() -> recordAnalyticsEventService.record(request))
                .isInstanceOf(InvalidAnalyticsEventException.class);
    }

    @Test
    @DisplayName("query returns events in requested date range")
    void query_all_success() {
        givenCurrentUser();
        LocalDate from = LocalDate.of(2026, 4, 29);
        LocalDate to = LocalDate.of(2026, 4, 30);
        Instant fromInstant = Instant.parse("2026-04-28T15:00:00Z");
        Instant toExclusive = Instant.parse("2026-04-30T15:00:00Z");
        AnalyticsEvent event = event(AnalyticsEventType.MORNING_TASK_CREATED, "source", "home");
        given(analyticsEventRepository.findAllByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
                userId,
                fromInstant,
                toExclusive
        )).willReturn(List.of(event));

        List<AnalyticsEventResponse> responses = queryAnalyticsEventService.query(from, to, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).eventType()).isEqualTo(AnalyticsEventType.MORNING_TASK_CREATED);
        assertThat(responses.get(0).properties()).containsEntry("source", "home");
    }

    @Test
    @DisplayName("query filters events by event type")
    void query_eventType_success() {
        givenCurrentUser();
        LocalDate from = LocalDate.of(2026, 4, 29);
        LocalDate to = LocalDate.of(2026, 4, 29);
        Instant fromInstant = Instant.parse("2026-04-28T15:00:00Z");
        Instant toExclusive = Instant.parse("2026-04-29T15:00:00Z");
        AnalyticsEvent event = event(AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED, "relation", "friend");
        given(analyticsEventRepository.findAllByUserIdAndEventTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
                userId,
                AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED,
                fromInstant,
                toExclusive
        )).willReturn(List.of(event));

        List<AnalyticsEventResponse> responses = queryAnalyticsEventService.query(
                from,
                to,
                AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).eventType()).isEqualTo(AnalyticsEventType.ACCOUNTABILITY_FRIEND_JOINED);
    }

    @Test
    @DisplayName("query rejects invalid date range")
    void query_invalidDateRange_throwsException() {
        assertThatThrownBy(() -> queryAnalyticsEventService.query(
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 29),
                null
        )).isInstanceOf(InvalidAnalyticsEventException.class);
    }

    private AnalyticsEvent event(AnalyticsEventType eventType, String key, String value) {
        return AnalyticsEvent.record(
                userId,
                eventType,
                clock.instant(),
                Map.of(key, value)
        );
    }
}

package com.example.movra.application.notification;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.QueryNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.UpdateNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.dto.request.NotificationPreferenceRequest;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.exception.InvalidNotificationPreferenceException;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final UserId userId = UserId.newId();
    private QueryNotificationPreferenceService queryNotificationPreferenceService;
    private UpdateNotificationPreferenceService updateNotificationPreferenceService;

    @BeforeEach
    void setUp() {
        queryNotificationPreferenceService = new QueryNotificationPreferenceService(
                notificationPreferenceRepository,
                currentUserQuery
        );
        updateNotificationPreferenceService = new UpdateNotificationPreferenceService(
                notificationPreferenceRepository,
                currentUserQuery,
                analyticsEventRecorder
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("queryMine creates default preference when missing")
    void queryMine_missing_createsDefault() {
        givenCurrentUser();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0, NotificationPreference.class));

        NotificationPreferenceResponse response = queryNotificationPreferenceService.queryMine();

        assertThat(response.dailyFocusEnabled()).isFalse();
        assertThat(response.schoolHoursQuietEnabled()).isTrue();
        assertThat(response.weekendSchoolQuietEnabled()).isFalse();
        assertThat(response.sleepHoursQuietEnabled()).isTrue();
        assertThat(response.maxDailyPushCount()).isEqualTo(NotificationPreference.DEFAULT_MAX_DAILY_PUSH_COUNT);
        then(notificationPreferenceRepository).should().save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("update creates preference when missing")
    void update_missing_createsAndUpdatesPreference() {
        givenCurrentUser();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0, NotificationPreference.class));

        NotificationPreferenceResponse response = updateNotificationPreferenceService.update(validRequest());

        assertThat(response.dailyFocusEnabled()).isTrue();
        assertThat(response.dailyTopPicksEnabled()).isTrue();
        assertThat(response.dailyTimetableEnabled()).isFalse();
        assertThat(response.accountabilityEnabled()).isTrue();
        assertThat(response.weekendSchoolQuietEnabled()).isTrue();
        assertThat(response.sleepHoursQuietEnabled()).isTrue();
        assertThat(response.maxDailyPushCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("update throws when daily push count is out of range")
    void update_invalidDailyPushCount_throwsException() {
        givenCurrentUser();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.empty());
        NotificationPreferenceRequest request = new NotificationPreferenceRequest(
                true,
                true,
                true,
                true,
                true,
                LocalTime.of(8, 0),
                LocalTime.of(15, 30),
                false,
                true,
                11
        );

        assertThatThrownBy(() -> updateNotificationPreferenceService.update(request))
                .isInstanceOf(InvalidNotificationPreferenceException.class);
    }

    @Test
    @DisplayName("update throws when sleep quiet hours are disabled")
    void update_sleepQuietDisabled_throwsException() {
        givenCurrentUser();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.empty());
        NotificationPreferenceRequest request = new NotificationPreferenceRequest(
                true,
                true,
                true,
                true,
                true,
                LocalTime.of(8, 0),
                LocalTime.of(15, 30),
                false,
                false,
                3
        );

        assertThatThrownBy(() -> updateNotificationPreferenceService.update(request))
                .isInstanceOf(InvalidNotificationPreferenceException.class);
    }

    @Test
    @DisplayName("update records SCHOOL_HOURS_MUTE_TOGGLED when school hours quiet flag changes")
    void update_schoolHoursQuietToggled_recordsAnalyticsEvent() {
        givenCurrentUser();
        NotificationPreference existing = NotificationPreference.createDefault(userId);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(existing));
        given(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0, NotificationPreference.class));

        NotificationPreferenceRequest request = new NotificationPreferenceRequest(
                true, true, false, true,
                false,
                LocalTime.of(8, 0),
                LocalTime.of(15, 30),
                true, true, 4
        );

        updateNotificationPreferenceService.update(request);

        then(analyticsEventRecorder).should()
                .recordSafely(eq(userId), eq(AnalyticsEventType.SCHOOL_HOURS_MUTE_TOGGLED), eq(Map.of("enabled", "false")));
    }

    @Test
    @DisplayName("update does not record analytics when school hours flag is unchanged")
    void update_schoolHoursQuietUnchanged_skipsAnalyticsEvent() {
        givenCurrentUser();
        NotificationPreference existing = NotificationPreference.createDefault(userId);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(existing));
        given(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0, NotificationPreference.class));

        updateNotificationPreferenceService.update(validRequest());

        then(analyticsEventRecorder).should(never())
                .recordSafely(eq(userId), eq(AnalyticsEventType.SCHOOL_HOURS_MUTE_TOGGLED), any());
    }

    private NotificationPreferenceRequest validRequest() {
        return new NotificationPreferenceRequest(
                true,
                true,
                false,
                true,
                true,
                LocalTime.of(8, 0),
                LocalTime.of(15, 30),
                true,
                true,
                4
        );
    }
}

package com.example.movra.application.account.user;

import com.example.movra.bc.account.user.application.user.QueryOnboardingContextService;
import com.example.movra.bc.account.user.application.user.dto.response.OnboardingContextResponse;
import com.example.movra.sharedkernel.notification.SchoolHoursPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class QueryOnboardingContextServiceTest {

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    @Test
    void query_duringSchoolHoursOnWeekday_signalsPendingSchoolHours() {
        // 2026-05-04 Mon 10:00 KST → school hours
        Clock clock = Clock.fixed(Instant.parse("2026-05-04T01:00:00Z"), zoneId);
        QueryOnboardingContextService service = new QueryOnboardingContextService(new SchoolHoursPolicy(clock));

        OnboardingContextResponse response = service.query();

        assertThat(response.pendingSchoolHours()).isTrue();
    }

    @Test
    void query_outsideSchoolHours_returnsFalse() {
        // 2026-05-04 Mon 17:00 KST → after school hours
        Clock clock = Clock.fixed(Instant.parse("2026-05-04T08:00:00Z"), zoneId);
        QueryOnboardingContextService service = new QueryOnboardingContextService(new SchoolHoursPolicy(clock));

        OnboardingContextResponse response = service.query();

        assertThat(response.pendingSchoolHours()).isFalse();
    }

    @Test
    void query_onWeekendDuringSchoolHourClock_returnsFalse() {
        // 2026-05-02 Sat 10:00 KST → not school day
        Clock clock = Clock.fixed(Instant.parse("2026-05-02T01:00:00Z"), zoneId);
        QueryOnboardingContextService service = new QueryOnboardingContextService(new SchoolHoursPolicy(clock));

        OnboardingContextResponse response = service.query();

        assertThat(response.pendingSchoolHours()).isFalse();
    }
}

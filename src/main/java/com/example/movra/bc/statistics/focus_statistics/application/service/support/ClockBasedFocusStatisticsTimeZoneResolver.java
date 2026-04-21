package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class ClockBasedFocusStatisticsTimeZoneResolver implements FocusStatisticsTimeZoneResolver {

    private final Clock clock;

    @Override
    public ZoneId resolve(UserId userId) {
        return clock.getZone();
    }
}

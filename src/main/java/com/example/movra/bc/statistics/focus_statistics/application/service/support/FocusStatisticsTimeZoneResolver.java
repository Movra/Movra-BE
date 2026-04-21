package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.ZoneId;

public interface FocusStatisticsTimeZoneResolver {

    ZoneId resolve(UserId userId);
}

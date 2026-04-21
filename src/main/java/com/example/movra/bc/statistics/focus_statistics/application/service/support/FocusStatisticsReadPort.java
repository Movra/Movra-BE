package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FocusStatisticsReadPort {

    Optional<FocusStatisticsSummaryView> findSummary(UserId userId, LocalDate date);

    List<FocusStatisticsSummaryView> findSummaryRange(UserId userId, LocalDate from, LocalDate to);

    List<FocusStatisticsSessionView> findSessions(UserId userId, FocusStatisticsPeriod period);
}

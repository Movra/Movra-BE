package com.example.movra.application.statistics.focus_statistics;

import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusPeriodStatisticsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusPeriodStatisticsResultTest {

    @Test
    @DisplayName("constructor rejects null period dates")
    void constructor_nullDates_throwsException() {
        assertThatThrownBy(() -> new FocusPeriodStatisticsResult(
                null,
                LocalDate.of(2026, 4, 10),
                1,
                1,
                1800L,
                FocusStatisticsStatus.FINAL,
                FocusStatisticsDataSource.SUMMARY
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("period dates must not be null");
    }

    @Test
    @DisplayName("averageDailyFocusSeconds uses covered day count")
    void averageDailyFocusSeconds_success() {
        FocusPeriodStatisticsResult result = new FocusPeriodStatisticsResult(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3),
                3,
                2,
                5400L,
                FocusStatisticsStatus.FINAL,
                FocusStatisticsDataSource.MIXED
        );

        assertThat(result.averageDailyFocusSeconds()).isEqualTo(2700L);
    }
}

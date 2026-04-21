package com.example.movra.application.statistics.focus_statistics;

import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusStatisticsPeriodTest {

    @Test
    @DisplayName("constructor rejects null dates")
    void constructor_nullDates_throwsException() {
        Instant startInstant = Instant.parse("2026-04-10T00:00:00Z");
        Instant endInstant = Instant.parse("2026-04-11T00:00:00Z");

        assertThatThrownBy(() -> new FocusStatisticsPeriod(
                null,
                LocalDate.of(2026, 4, 10),
                startInstant,
                endInstant,
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("dates must not be null");
    }

    @Test
    @DisplayName("constructor rejects day count that does not match the inclusive date range")
    void constructor_invalidDayCount_throwsException() {
        assertThatThrownBy(() -> new FocusStatisticsPeriod(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3),
                Instant.parse("2026-03-31T15:00:00Z"),
                Instant.parse("2026-04-03T15:00:00Z"),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("dayCount must match the inclusive date range");
    }

    @Test
    @DisplayName("constructor accepts a valid inclusive date range")
    void constructor_validRange_success() {
        assertThatCode(() -> new FocusStatisticsPeriod(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3),
                Instant.parse("2026-03-31T15:00:00Z"),
                Instant.parse("2026-04-03T15:00:00Z"),
                3
        )).doesNotThrowAnyException();
    }
}

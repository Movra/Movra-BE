package com.example.movra.bc.insight.behavior_insight.domain.vo;

import com.example.movra.bc.insight.behavior_insight.domain.exception.InvalidAnalysisPeriodException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 분석 대상 기간(롤링 30일 등). periodStart, periodEnd 모두 포함(inclusive)이다.
 */
@Embeddable
public record AnalysisPeriod(
        @Column(name = "period_start", nullable = false)
        LocalDate periodStart,

        @Column(name = "period_end", nullable = false)
        LocalDate periodEnd
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public AnalysisPeriod {
        if (periodStart == null || periodEnd == null || periodStart.isAfter(periodEnd)) {
            throw new InvalidAnalysisPeriodException();
        }
    }

    /**
     * endInclusive를 마지막 날로 하는 days일 기간을 만든다. (예: 30일 윈도우)
     */
    public static AnalysisPeriod lastDays(LocalDate endInclusive, int days) {
        if (endInclusive == null || days <= 0) {
            throw new InvalidAnalysisPeriodException();
        }
        return new AnalysisPeriod(endInclusive.minusDays(days - 1L), endInclusive);
    }

    public long days() {
        return ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
    }
}

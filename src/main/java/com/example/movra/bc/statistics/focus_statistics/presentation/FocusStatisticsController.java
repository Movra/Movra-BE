package com.example.movra.bc.statistics.focus_statistics.presentation;

import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusPeriodStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusTimeOfDayStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.RecommendFocusTimingService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusPeriodStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeOfDayStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/focus-statistics")
@RequiredArgsConstructor
public class FocusStatisticsController {

    private final QueryFocusPeriodStatisticsService queryFocusPeriodStatisticsService;
    private final QueryFocusTimeOfDayStatisticsService queryFocusTimeOfDayStatisticsService;
    private final RecommendFocusTimingService recommendFocusTimingService;

    @GetMapping("/daily")
    public FocusPeriodStatisticsResponse queryDaily(@RequestParam LocalDate targetDate) {
        return queryFocusPeriodStatisticsService.queryDaily(targetDate);
    }

    @GetMapping("/weekly")
    public FocusPeriodStatisticsResponse queryWeekly(@RequestParam LocalDate targetDate) {
        return queryFocusPeriodStatisticsService.queryWeekly(targetDate);
    }

    @GetMapping("/monthly")
    public FocusPeriodStatisticsResponse queryMonthly(@RequestParam LocalDate targetDate) {
        return queryFocusPeriodStatisticsService.queryMonthly(targetDate);
    }

    @GetMapping("/time-of-day")
    public FocusTimeOfDayStatisticsResponse queryTimeOfDay(@RequestParam LocalDate targetDate) {
        return queryFocusTimeOfDayStatisticsService.query(targetDate);
    }

    @GetMapping("/timing-recommendation")
    public FocusTimingRecommendationResponse recommendTiming() {
        return recommendFocusTimingService.recommend();
    }
}

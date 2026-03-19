package com.example.movra.bc.planning.daily_plan.presentation;

import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.DailyPlanCreateService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.DailyPlanQueryService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.request.DailyPlanRequest;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.DailyPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController {

    private final DailyPlanCreateService dailyPlanCreateService;
    private final DailyPlanQueryService dailyPlanQueryService;

    @PostMapping
    public void create(@Valid @RequestBody DailyPlanRequest request) {
        dailyPlanCreateService.create(request);
    }

    @GetMapping
    public DailyPlanResponse findByPlanDate(@RequestParam LocalDate planDate) {
        return dailyPlanQueryService.findByPlanDate(planDate);
    }
}

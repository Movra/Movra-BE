package com.example.movra.bc.feedback.daily_reflection.presentation;

import com.example.movra.bc.feedback.daily_reflection.application.service.CreateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.QueryDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.UpdateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.CreateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.UpdateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.response.DailyReflectionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/daily-reflections")
@RequiredArgsConstructor
public class DailyReflectionController {

    private final CreateDailyReflectionService createDailyReflectionService;
    private final QueryDailyReflectionService queryDailyReflectionService;
    private final UpdateDailyReflectionService updateDailyReflectionService;

    @PostMapping
    public void create(@Valid @RequestBody CreateDailyReflectionRequest request) {
        createDailyReflectionService.create(request);
    }

    @GetMapping
    public DailyReflectionResponse query(@RequestParam LocalDate targetDate) {
        return queryDailyReflectionService.query(targetDate);
    }

    @PatchMapping("/{dailyReflectionId}")
    public void update(
            @PathVariable UUID dailyReflectionId,
            @Valid @RequestBody UpdateDailyReflectionRequest request
    ) {
        updateDailyReflectionService.update(dailyReflectionId, request);
    }
}

package com.example.movra.bc.visioning.future_vision.presentation;

import com.example.movra.bc.visioning.future_vision.application.service.CreateFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.QueryFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.UpdateWeeklyVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.UpdateYearlyVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateWeeklyVisionRequest;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateYearlyVisionRequest;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.FutureVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.WeeklyVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.YearlyVisionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/future-vision")
@RequiredArgsConstructor
public class FutureVisionController {

    private final CreateFutureVisionService createFutureVisionService;
    private final QueryFutureVisionService queryFutureVisionService;
    private final UpdateWeeklyVisionService updateWeeklyVisionService;
    private final UpdateYearlyVisionService updateYearlyVisionService;

    @PostMapping
    public void create(@Valid @RequestBody CreateFutureVisionRequest request) {
        createFutureVisionService.create(request);
    }

    @GetMapping
    public FutureVisionResponse query() {
        return queryFutureVisionService.query();
    }

    @GetMapping("/weekly")
    public WeeklyVisionResponse queryWeeklyVision() {
        return queryFutureVisionService.queryWeeklyVision();
    }

    @GetMapping("/yearly")
    public YearlyVisionResponse queryYearlyVision() {
        return queryFutureVisionService.queryYearlyVision();
    }

    @PatchMapping("/weekly")
    public void updateWeekly(@Valid @RequestBody UpdateWeeklyVisionRequest request) {
        updateWeeklyVisionService.update(request);
    }

    @PatchMapping("/yearly")
    public void updateYearly(@Valid @RequestBody UpdateYearlyVisionRequest request) {
        updateYearlyVisionService.update(request);
    }
}

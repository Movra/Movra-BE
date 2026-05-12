package com.example.movra.bc.planning.exam_schedule.presentation;

import com.example.movra.bc.planning.exam_schedule.application.service.CreateExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.DeleteExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.QueryExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.QuerySeasonModeService;
import com.example.movra.bc.planning.exam_schedule.application.service.UpdateExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.request.ExamScheduleRequest;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.SeasonModeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final CreateExamScheduleService createExamScheduleService;
    private final QueryExamScheduleService queryExamScheduleService;
    private final QuerySeasonModeService querySeasonModeService;
    private final UpdateExamScheduleService updateExamScheduleService;
    private final DeleteExamScheduleService deleteExamScheduleService;

    @PostMapping
    public ExamScheduleResponse create(@Valid @RequestBody ExamScheduleRequest request) {
        return createExamScheduleService.create(request);
    }

    @GetMapping
    public List<ExamScheduleResponse> queryAll() {
        return queryExamScheduleService.queryAll();
    }

    @GetMapping("/next")
    public ExamScheduleResponse queryNext() {
        return queryExamScheduleService.queryNext();
    }

    @GetMapping("/season-mode")
    public SeasonModeResponse querySeasonMode() {
        return querySeasonModeService.queryMine();
    }

    @GetMapping("/{examScheduleId}")
    public ExamScheduleResponse query(@PathVariable UUID examScheduleId) {
        return queryExamScheduleService.query(examScheduleId);
    }

    @PatchMapping("/{examScheduleId}")
    public ExamScheduleResponse update(
            @PathVariable UUID examScheduleId,
            @Valid @RequestBody ExamScheduleRequest request
    ) {
        return updateExamScheduleService.update(examScheduleId, request);
    }

    @DeleteMapping("/{examScheduleId}")
    public void delete(@PathVariable UUID examScheduleId) {
        deleteExamScheduleService.delete(examScheduleId);
    }
}

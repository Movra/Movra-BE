package com.example.movra.bc.planning.daily_plan.presentation;

import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response.MindSweepResponse;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.AddMorningTaskService;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.CompleteMorningTaskService;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.DeleteMorningTaskService;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.QueryMorningTaskService;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.UnCompleteMorningTaskService;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.UpdateMorningTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/morning-tasks")
@RequiredArgsConstructor
public class MorningTaskController {

    private final QueryMorningTaskService queryMorningTaskService;
    private final AddMorningTaskService addMorningTaskService;
    private final UpdateMorningTaskService updateMorningTaskService;
    private final DeleteMorningTaskService deleteMorningTaskService;
    private final CompleteMorningTaskService completeMorningTaskService;
    private final UnCompleteMorningTaskService unCompleteMorningTaskService;

    @GetMapping
    public List<MindSweepResponse> queryAll(@RequestParam LocalDate targetDate) {
        return queryMorningTaskService.queryAll(targetDate);
    }

    @PostMapping
    public void create(@RequestParam LocalDate targetDate, @Valid @RequestBody MindSweepRequest request) {
        addMorningTaskService.create(request, targetDate);
    }

    @PutMapping("/{dailyPlanId}/{taskId}")
    public void update(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId, @Valid @RequestBody MindSweepRequest request) {
        updateMorningTaskService.update(request, dailyPlanId, taskId);
    }

    @DeleteMapping("/{dailyPlanId}/{taskId}")
    public void delete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        deleteMorningTaskService.delete(dailyPlanId, taskId);
    }

    @PatchMapping("/{dailyPlanId}/{taskId}/complete")
    public void complete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        completeMorningTaskService.complete(dailyPlanId, taskId);
    }

    @PatchMapping("/{dailyPlanId}/{taskId}/uncomplete")
    public void unComplete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        unCompleteMorningTaskService.unComplete(dailyPlanId, taskId);
    }
}

package com.example.movra.bc.planning.daily_plan.presentation;

import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.AddMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.CompleteMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.DeleteMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.QueryMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.UnCompleteMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.UpdateMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response.MindSweepResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/daily-plans/{dailyPlanId}/mind-sweeps")
@RequiredArgsConstructor
public class MindSweepController {

    private final QueryMindSweepService queryMindSweepService;
    private final AddMindSweepService addMindSweepService;
    private final UpdateMindSweepService updateMindSweepService;
    private final DeleteMindSweepService deleteMindSweepService;
    private final CompleteMindSweepService completeMindSweepService;
    private final UnCompleteMindSweepService unCompleteMindSweepService;

    @GetMapping
    public List<MindSweepResponse> queryAll(@PathVariable UUID dailyPlanId) {
        return queryMindSweepService.queryAll(dailyPlanId);
    }

    @PostMapping
    public void create(@PathVariable UUID dailyPlanId, @Valid @RequestBody MindSweepRequest request) {
        addMindSweepService.create(request, dailyPlanId);
    }

    @PutMapping("/{taskId}")
    public void update(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId, @Valid @RequestBody MindSweepRequest request) {
        updateMindSweepService.update(request, dailyPlanId, taskId);
    }

    @DeleteMapping("/{taskId}")
    public void delete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        deleteMindSweepService.delete(dailyPlanId, taskId);
    }

    @PatchMapping("/{taskId}/complete")
    public void complete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        completeMindSweepService.complete(dailyPlanId, taskId);
    }

    @PatchMapping("/{taskId}/uncomplete")
    public void unComplete(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        unCompleteMindSweepService.unComplete(dailyPlanId, taskId);
    }
}

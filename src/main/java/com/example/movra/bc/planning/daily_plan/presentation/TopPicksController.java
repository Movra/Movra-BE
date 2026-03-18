package com.example.movra.bc.planning.daily_plan.presentation;

import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.DeselectTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.QueryTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.SelectTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request.TopPicksRequest;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/daily-plans/{dailyPlanId}/top-picks")
@RequiredArgsConstructor
public class TopPicksController {

    private final QueryTopPicksService queryTopPicksService;
    private final SelectTopPicksService selectTopPicksService;
    private final DeselectTopPicksService deselectTopPicksService;

    @GetMapping
    public List<TopPicksResponse> queryAll(@PathVariable UUID dailyPlanId) {
        return queryTopPicksService.queryAll(dailyPlanId);
    }

    @PostMapping("/{taskId}")
    public void select(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId, @Valid @RequestBody TopPicksRequest request) {
        selectTopPicksService.select(request, dailyPlanId, taskId);
    }

    @DeleteMapping("/{taskId}")
    public void deselect(@PathVariable UUID dailyPlanId, @PathVariable UUID taskId) {
        deselectTopPicksService.deselect(dailyPlanId, taskId);
    }
}

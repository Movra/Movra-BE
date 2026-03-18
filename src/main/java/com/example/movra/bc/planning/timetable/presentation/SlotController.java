package com.example.movra.bc.planning.timetable.presentation;

import com.example.movra.bc.planning.timetable.application.service.AddDirectSlotService;
import com.example.movra.bc.planning.timetable.application.service.AssignTaskSlotService;
import com.example.movra.bc.planning.timetable.application.service.AssignTopPickSlotService;
import com.example.movra.bc.planning.timetable.application.service.RemoveSlotService;
import com.example.movra.bc.planning.timetable.application.service.RescheduleSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AddDirectSlotRequest;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTaskSlotRequest;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTopPickSlotRequest;
import com.example.movra.bc.planning.timetable.application.service.dto.request.RescheduleSlotRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/timetables/{timetableId}/slots")
@RequiredArgsConstructor
public class SlotController {

    private final AssignTopPickSlotService assignTopPickSlotService;
    private final AssignTaskSlotService assignTaskSlotService;
    private final AddDirectSlotService addDirectSlotService;
    private final RescheduleSlotService rescheduleSlotService;
    private final RemoveSlotService removeSlotService;

    @PostMapping("/tasks/{taskId}/top-picks")
    public void assignTopPick(
            @PathVariable UUID timetableId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTopPickSlotRequest request
    ) {
        assignTopPickSlotService.assign(timetableId, taskId, request);
    }

    @PostMapping("/task/{taskId}")
    public void assignTask(
            @PathVariable UUID timetableId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskSlotRequest request
    ) {
        assignTaskSlotService.assign(timetableId, taskId, request);
    }

    @PostMapping("/daily-plans/{dailyPlanId}/direct")
    public void addDirect(
            @PathVariable UUID timetableId,
            @PathVariable UUID dailyPlanId,
            @Valid @RequestBody AddDirectSlotRequest request
    ) {
        addDirectSlotService.execute(timetableId, dailyPlanId, request);
    }

    @PatchMapping("/{slotId}/reschedule")
    public void reschedule(
            @PathVariable UUID timetableId,
            @PathVariable UUID slotId,
            @Valid @RequestBody RescheduleSlotRequest request
    ) {
        rescheduleSlotService.reschedule(timetableId, slotId, request);
    }

    @DeleteMapping("/{slotId}")
    public void remove(
            @PathVariable UUID timetableId,
            @PathVariable UUID slotId
    ) {
        removeSlotService.remove(timetableId, slotId);
    }
}

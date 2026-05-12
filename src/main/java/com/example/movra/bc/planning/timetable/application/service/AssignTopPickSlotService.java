package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTopPickSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Slot;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignTopPickSlotService {

    private final TimetableRepository timetableRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final NotificationGateway notificationGateway;

    @Transactional
    public void assign(UUID timetableId, UUID taskId, AssignTopPickSlotRequest request) {
        Timetable timetable = timetableRepository.findById(TimetableId.of(timetableId))
                .orElseThrow(TimetableNotFoundException::new);

        UserId userId = currentUserQuery.currentUser().userId();
        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(timetable.getDailyPlanId(), userId)
                .orElseThrow(DailyPlanNotFoundException::new);
        TaskId selectedTaskId = TaskId.of(taskId);
        dailyPlan.validateTopPickedTask(selectedTaskId);

        Slot slot = timetable.assignTopPick(
                selectedTaskId,
                request.startTime(),
                request.endTime()
        );

        timetableRepository.save(timetable);

        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.TIMETABLE_SLOT_CREATED,
                Map.of(
                        "dailyPlanId", timetable.getDailyPlanId().id().toString(),
                        "timetableId", timetableId.toString(),
                        "taskId", taskId.toString(),
                        "slotId", slot.getSlotId().id().toString(),
                        "slotType", "TOP_PICK",
                        "startTime", request.startTime().toString(),
                        "endTime", request.endTime().toString()
                )
        );
        notificationGateway.sendSafely(
                userId,
                NotificationPayload.of(
                        NotificationType.DAILY_TIMETABLE,
                        "Top Pick 시간 배정",
                        "오늘 시간표에 핵심 과제가 배정됐어요.",
                        Map.of(
                                "dailyPlanId", timetable.getDailyPlanId().id().toString(),
                                "timetableId", timetableId.toString(),
                                "taskId", taskId.toString(),
                                "slotId", slot.getSlotId().id().toString(),
                                "slotType", "TOP_PICK"
                        )
                )
        );
    }
}

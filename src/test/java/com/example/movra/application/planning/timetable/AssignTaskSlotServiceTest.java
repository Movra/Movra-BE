package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.AssignTaskSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTaskSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.exception.TopPicksNotFullyAssignedException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AssignTaskSlotServiceTest {

    @InjectMocks
    private AssignTaskSlotService assignTaskSlotService;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Mock
    private NotificationGateway notificationGateway;

    private final UserId userId = UserId.newId();

    @BeforeEach
    void setUp() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build());
    }

    private void stubOwnership(Timetable timetable, DailyPlan dailyPlan) {
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(timetable.getDailyPlanId(), userId)).willReturn(Optional.of(dailyPlan));
    }

    private DailyPlan createDailyPlan() {
        return DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
    }

    @Test
    @DisplayName("일반 Task 슬롯 배정 성공 (TopPick이 없는 경우)")
    void assign_success() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task task = dailyPlan.addTask("일반 과제");
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 0);
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = task.getTaskId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when
        assignTaskSlotService.assign(timetableId, taskId,
                new AssignTaskSlotRequest(LocalTime.of(11, 0), LocalTime.of(12, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(1);
        assertThat(timetable.getSlots().get(0).isTopPick()).isFalse();
        assertThat(timetable.getSlots().get(0).getTaskId()).isEqualTo(TaskId.of(taskId));
        then(timetableRepository).should().save(timetable);
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.TIMETABLE_SLOT_CREATED),
                argThat(properties ->
                        properties.get("dailyPlanId").equals(timetable.getDailyPlanId().id().toString())
                                && properties.get("timetableId").equals(timetableId.toString())
                                && properties.get("taskId").equals(taskId.toString())
                                && properties.get("slotId").equals(timetable.getSlots().get(0).getSlotId().id().toString())
                                && properties.get("slotType").equals("TASK_ASSIGNED")
                )
        );
        then(notificationGateway).should().sendSafely(
                eq(userId),
                argThat(payload ->
                        payload.type().name().equals("DAILY_TIMETABLE")
                                && payload.data().get("slotId").equals(timetable.getSlots().get(0).getSlotId().id().toString())
                                && payload.data().get("slotType").equals("TASK_ASSIGNED")
                )
        );
    }

    @Test
    @DisplayName("TopPick 전체 배정 후 일반 Task 배정 성공")
    void assign_afterTopPicksAssigned_success() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task topPickedTask = dailyPlan.addTask("핵심 과제");
        Task task = dailyPlan.addTask("일반 과제");
        dailyPlan.markAsTopPicked(topPickedTask.getTaskId(), 60, "memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        timetable.assignTopPick(topPickedTask.getTaskId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = task.getTaskId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when
        assignTaskSlotService.assign(timetableId, taskId,
                new AssignTaskSlotRequest(LocalTime.of(11, 0), LocalTime.of(12, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(2);
    }

    @Test
    @DisplayName("TopPick 미배정 상태에서 일반 Task 배정 시 TopPicksNotFullyAssignedException 발생")
    void assign_topPicksNotAssigned_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task task = dailyPlan.addTask("일반 과제");
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 2);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTaskSlotService.assign(timetableId, task.getTaskId().id(),
                new AssignTaskSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TopPicksNotFullyAssignedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable에 Task 배정 시 TimetableNotFoundException 발생")
    void assign_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assignTaskSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTaskSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("DailyPlan에 없는 Task를 슬롯으로 배정하면 TaskNotFoundException 발생")
    void assign_taskNotInDailyPlan_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 0);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTaskSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTaskSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TaskNotFoundException.class);
    }
}

package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.exception.NotTopPickedTaskException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.AssignTopPickSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTopPickSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.InvalidTimeRangeException;
import com.example.movra.bc.planning.timetable.domain.exception.TimeOverlapException;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
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
class AssignTopPickSlotServiceTest {

    @InjectMocks
    private AssignTopPickSlotService assignTopPickSlotService;

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

    private Task addTopPickedTask(DailyPlan dailyPlan) {
        Task task = dailyPlan.addTask("핵심 과제");
        dailyPlan.markAsTopPicked(task.getTaskId(), 60, "memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        return task;
    }

    @Test
    @DisplayName("TopPick 슬롯 배정 성공")
    void assign_success() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task task = addTopPickedTask(dailyPlan);
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = task.getTaskId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when
        assignTopPickSlotService.assign(timetableId, taskId,
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(1);
        assertThat(timetable.getSlots().get(0).isTopPick()).isTrue();
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
                                && properties.get("slotType").equals("TOP_PICK")
                )
        );
        then(notificationGateway).should().sendSafely(
                eq(userId),
                argThat(payload ->
                        payload.type().name().equals("DAILY_TIMETABLE")
                                && payload.data().get("slotId").equals(timetable.getSlots().get(0).getSlotId().id().toString())
                                && payload.data().get("slotType").equals("TOP_PICK")
                )
        );
    }

    @Test
    @DisplayName("존재하지 않는 Timetable에 TopPick 배정 시 TimetableNotFoundException 발생")
    void assign_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("시간 겹침 시 TimeOverlapException 발생")
    void assign_timeOverlap_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task assignedTask = addTopPickedTask(dailyPlan);
        Task overlappingTask = addTopPickedTask(dailyPlan);
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 2);
        UUID timetableId = timetable.getTimetableId().id();
        timetable.assignTopPick(assignedTask.getTaskId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, overlappingTask.getTaskId().id(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 30), LocalTime.of(10, 30))))
                .isInstanceOf(TimeOverlapException.class);
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦으면 InvalidTimeRangeException 발생")
    void assign_invalidTimeRange_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task task = addTopPickedTask(dailyPlan);
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, task.getTaskId().id(),
                new AssignTopPickSlotRequest(LocalTime.of(10, 0), LocalTime.of(9, 0))))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    @DisplayName("DailyPlan에 없는 Task를 TopPick 슬롯으로 배정하면 TaskNotFoundException 발생")
    void assign_taskNotInDailyPlan_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("TopPick으로 선택되지 않은 Task를 TopPick 슬롯으로 배정하면 NotTopPickedTaskException 발생")
    void assign_notTopPickedTask_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        Task task = dailyPlan.addTask("일반 과제");
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable, dailyPlan);

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, task.getTaskId().id(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(NotTopPickedTaskException.class);
    }
}

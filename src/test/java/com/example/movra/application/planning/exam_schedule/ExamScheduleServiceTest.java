package com.example.movra.application.planning.exam_schedule;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.planning.exam_schedule.application.exception.ExamScheduleNotFoundException;
import com.example.movra.bc.planning.exam_schedule.application.service.CreateExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.DeleteExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.QueryExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.QuerySeasonModeService;
import com.example.movra.bc.planning.exam_schedule.application.service.UpdateExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.request.ExamScheduleRequest;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.SeasonModeResponse;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.exception.InvalidExamScheduleException;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExamScheduleServiceTest {

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();
    private final LocalDate today = LocalDate.of(2026, 4, 29);

    private CreateExamScheduleService createExamScheduleService;
    private QueryExamScheduleService queryExamScheduleService;
    private QuerySeasonModeService querySeasonModeService;
    private UpdateExamScheduleService updateExamScheduleService;
    private DeleteExamScheduleService deleteExamScheduleService;

    @BeforeEach
    void setUp() {
        createExamScheduleService = new CreateExamScheduleService(
                examScheduleRepository,
                currentUserQuery,
                clock,
                analyticsEventRecorder
        );
        queryExamScheduleService = new QueryExamScheduleService(
                examScheduleRepository,
                currentUserQuery,
                clock
        );
        querySeasonModeService = new QuerySeasonModeService(
                examScheduleRepository,
                currentUserQuery,
                clock
        );
        updateExamScheduleService = new UpdateExamScheduleService(
                examScheduleRepository,
                currentUserQuery,
                clock
        );
        deleteExamScheduleService = new DeleteExamScheduleService(
                examScheduleRepository,
                currentUserQuery
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("create succeeds")
    void create_success() {
        givenCurrentUser();
        ExamScheduleRequest request = request(ExamType.NAESIN, "중간고사", today.plusDays(14), "수학");
        given(examScheduleRepository.save(any(ExamSchedule.class)))
                .willAnswer(invocation -> invocation.getArgument(0, ExamSchedule.class));

        ExamScheduleResponse response = createExamScheduleService.create(request);

        assertThat(response.examType()).isEqualTo(ExamType.NAESIN);
        assertThat(response.title()).isEqualTo("중간고사");
        assertThat(response.examDate()).isEqualTo(today.plusDays(14));
        assertThat(response.subject()).isEqualTo("수학");
        assertThat(response.daysUntil()).isEqualTo(14);
        assertThat(response.seasonMode()).isEqualTo(SeasonMode.NAESIN_INTENSIVE);
        then(examScheduleRepository).should().save(any(ExamSchedule.class));
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.EXAM_REGISTERED),
                argThat(properties ->
                        properties.containsKey("examScheduleId")
                                && properties.get("examType").equals(ExamType.NAESIN.name())
                                && properties.get("examDate").equals(today.plusDays(14).toString())
                                && properties.get("subject").equals(request.subject())
                )
        );
    }

    @Test
    @DisplayName("create trims title and subject")
    void create_trimmedInput_success() {
        givenCurrentUser();
        ExamScheduleRequest request = request(ExamType.MOPYUNG, "  6월 모평  ", today.plusDays(30), "  국어  ");
        given(examScheduleRepository.save(any(ExamSchedule.class)))
                .willAnswer(invocation -> invocation.getArgument(0, ExamSchedule.class));

        ExamScheduleResponse response = createExamScheduleService.create(request);

        assertThat(response.title()).isEqualTo("6월 모평");
        assertThat(response.subject()).isEqualTo("국어");
    }

    @Test
    @DisplayName("create throws when required field is missing")
    void create_missingField_throwsException() {
        givenCurrentUser();
        ExamScheduleRequest request = request(null, "중간고사", today.plusDays(14), "수학");

        assertThatThrownBy(() -> createExamScheduleService.create(request))
                .isInstanceOf(InvalidExamScheduleException.class);
    }

    @Test
    @DisplayName("queryAll returns schedules ordered by date from repository")
    void queryAll_success() {
        givenCurrentUser();
        ExamSchedule first = schedule(ExamType.NAESIN, "중간고사", today.plusDays(7), "영어");
        ExamSchedule second = schedule(ExamType.MOPYUNG, "6월 모평", today.plusDays(30), null);
        given(examScheduleRepository.findAllByUserIdOrderByExamDateAsc(userId))
                .willReturn(List.of(first, second));

        List<ExamScheduleResponse> responses = queryExamScheduleService.queryAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).title()).isEqualTo("중간고사");
        assertThat(responses.get(0).daysUntil()).isEqualTo(7);
        assertThat(responses.get(0).seasonMode()).isEqualTo(SeasonMode.NAESIN_INTENSIVE);
        assertThat(responses.get(1).title()).isEqualTo("6월 모평");
        assertThat(responses.get(1).daysUntil()).isEqualTo(30);
        assertThat(responses.get(1).seasonMode()).isEqualTo(SeasonMode.BASELINE_MODE);
    }

    @Test
    @DisplayName("query succeeds")
    void query_success() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.SUNUNG, "수능", today.plusDays(200), null);
        UUID examScheduleId = examSchedule.getExamScheduleId().id();
        given(examScheduleRepository.findByExamScheduleIdAndUserId(ExamScheduleId.of(examScheduleId), userId))
                .willReturn(Optional.of(examSchedule));

        ExamScheduleResponse response = queryExamScheduleService.query(examScheduleId);

        assertThat(response.examScheduleId()).isEqualTo(examScheduleId);
        assertThat(response.examType()).isEqualTo(ExamType.SUNUNG);
        assertThat(response.seasonMode()).isEqualTo(SeasonMode.BASELINE_MODE);
    }

    @Test
    @DisplayName("query throws when schedule does not belong to current user")
    void query_notFound_throwsException() {
        givenCurrentUser();
        UUID examScheduleId = UUID.randomUUID();
        given(examScheduleRepository.findByExamScheduleIdAndUserId(ExamScheduleId.of(examScheduleId), userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> queryExamScheduleService.query(examScheduleId))
                .isInstanceOf(ExamScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("queryNext returns nearest upcoming exam")
    void queryNext_success() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.HAKPYUNG, "학평", today.plusDays(3), null);
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.of(examSchedule));

        ExamScheduleResponse response = queryExamScheduleService.queryNext();

        assertThat(response.title()).isEqualTo("학평");
        assertThat(response.daysUntil()).isEqualTo(3);
        assertThat(response.seasonMode()).isEqualTo(SeasonMode.MOPYUNG_FOCUSED);
    }

    @Test
    @DisplayName("queryNext throws when no upcoming exam exists")
    void queryNext_notFound_throwsException() {
        givenCurrentUser();
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> queryExamScheduleService.queryNext())
                .isInstanceOf(ExamScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("findNextForHome returns the upcoming exam when present")
    void findNextForHome_present_returnsResponse() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.HAKPYUNG, "학평", today.plusDays(3), null);
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.of(examSchedule));

        Optional<ExamScheduleResponse> response = queryExamScheduleService.findNextForHome();

        assertThat(response).isPresent();
        assertThat(response.get().title()).isEqualTo("학평");
        assertThat(response.get().daysUntil()).isEqualTo(3);
    }

    @Test
    @DisplayName("findNextForHome returns empty when no upcoming exam exists")
    void findNextForHome_notFound_returnsEmpty() {
        givenCurrentUser();
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.empty());

        assertThat(queryExamScheduleService.findNextForHome()).isEmpty();
    }

    @Test
    @DisplayName("querySeasonMode returns intensive mode for upcoming exam")
    void querySeasonMode_upcomingExam_returnsSeasonMode() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.SUNUNG, "?섎뒫", today.plusDays(30), null);
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.of(examSchedule));

        SeasonModeResponse response = querySeasonModeService.queryMine();

        assertThat(response.seasonMode()).isEqualTo(SeasonMode.SUNUNG_INTENSIVE);
        assertThat(response.nextExamSchedule()).isNotNull();
        assertThat(response.nextExamSchedule().daysUntil()).isEqualTo(30);
    }

    @Test
    @DisplayName("querySeasonMode returns baseline when no upcoming exam exists")
    void querySeasonMode_noUpcomingExam_returnsBaseline() {
        givenCurrentUser();
        given(examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today))
                .willReturn(Optional.empty());

        SeasonModeResponse response = querySeasonModeService.queryMine();

        assertThat(response.seasonMode()).isEqualTo(SeasonMode.BASELINE_MODE);
        assertThat(response.nextExamSchedule()).isNull();
    }

    @Test
    @DisplayName("update succeeds")
    void update_success() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.NAESIN, "중간고사", today.plusDays(7), "영어");
        UUID examScheduleId = examSchedule.getExamScheduleId().id();
        ExamScheduleRequest request = request(ExamType.MOPYUNG, "6월 모평", today.plusDays(40), "국어");
        given(examScheduleRepository.findByExamScheduleIdAndUserId(ExamScheduleId.of(examScheduleId), userId))
                .willReturn(Optional.of(examSchedule));
        given(examScheduleRepository.save(examSchedule)).willReturn(examSchedule);

        ExamScheduleResponse response = updateExamScheduleService.update(examScheduleId, request);

        assertThat(response.examType()).isEqualTo(ExamType.MOPYUNG);
        assertThat(response.title()).isEqualTo("6월 모평");
        assertThat(response.examDate()).isEqualTo(today.plusDays(40));
        assertThat(response.subject()).isEqualTo("국어");
        then(examScheduleRepository).should().save(examSchedule);
    }

    @Test
    @DisplayName("delete succeeds")
    void delete_success() {
        givenCurrentUser();
        ExamSchedule examSchedule = schedule(ExamType.OTHER, "학원 테스트", today.plusDays(5), null);
        UUID examScheduleId = examSchedule.getExamScheduleId().id();
        given(examScheduleRepository.findByExamScheduleIdAndUserId(ExamScheduleId.of(examScheduleId), userId))
                .willReturn(Optional.of(examSchedule));

        deleteExamScheduleService.delete(examScheduleId);

        then(examScheduleRepository).should().delete(examSchedule);
    }

    private ExamScheduleRequest request(ExamType examType, String title, LocalDate examDate, String subject) {
        return new ExamScheduleRequest(examType, title, examDate, subject);
    }

    private ExamSchedule schedule(ExamType examType, String title, LocalDate examDate, String subject) {
        return ExamSchedule.create(userId, examType, title, examDate, subject, clock);
    }
}

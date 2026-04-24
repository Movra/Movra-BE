package com.example.movra.presentation.accountability.accountability_relation;

import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherFocusSessionService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherTimetableTaskService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherTopPicksService;
import com.example.movra.bc.accountability.accountability_relation.presentation.WatcherQueryController;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryItemView;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryItemView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryItemView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WatcherQueryControllerTest {

    @Mock
    private QueryWatcherFocusSessionService queryWatcherFocusSessionService;

    @Mock
    private QueryWatcherTopPicksService queryWatcherTopPicksService;

    @Mock
    private QueryWatcherTimetableTaskService queryWatcherTimetableTaskService;

    private MockMvc mockMvc;

    private final UserId subjectUserId = UserId.newId();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        WatcherQueryController controller = new WatcherQueryController(
                queryWatcherFocusSessionService,
                queryWatcherTopPicksService,
                queryWatcherTimetableTaskService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("queryFocusSession returns focus summary for a date")
    void queryFocusSession_returnsSummary() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(queryWatcherFocusSessionService.query(date)).willReturn(
                Optional.of(new DailyFocusSummaryView(
                        subjectUserId, date, 3600L, 2,
                        List.of(new DailyFocusSummaryItemView(
                                Instant.parse("2026-04-20T01:00:00Z"),
                                Instant.parse("2026-04-20T01:30:00Z"),
                                1800L,
                                Instant.parse("2026-04-20T01:00:00Z"),
                                Instant.parse("2026-04-20T01:30:00Z"),
                                1800L, 0
                        ))
                ))
        );

        mockMvc.perform(get("/accountability-relations/watcher/focus-sessions")
                        .param("date", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSeconds").value(3600))
                .andExpect(jsonPath("$.sessionCount").value(2));
    }

    @Test
    @DisplayName("queryFocusSession returns 204 when no data")
    void queryFocusSession_noData_returns204() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(queryWatcherFocusSessionService.query(date)).willReturn(Optional.empty());

        mockMvc.perform(get("/accountability-relations/watcher/focus-sessions")
                        .param("date", "2026-04-20"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("queryFocusSessionRange returns focus summaries for range")
    void queryFocusSessionRange_returnsList() throws Exception {
        LocalDate from = LocalDate.of(2026, 4, 18);
        LocalDate to = LocalDate.of(2026, 4, 20);
        given(queryWatcherFocusSessionService.queryRange(from, to)).willReturn(
                List.of(
                        new DailyFocusSummaryView(subjectUserId, from, 1800L, 1, List.of()),
                        new DailyFocusSummaryView(subjectUserId, to, 3600L, 2, List.of())
                )
        );

        mockMvc.perform(get("/accountability-relations/watcher/focus-sessions/range")
                        .param("from", "2026-04-18")
                        .param("to", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].totalSeconds").value(1800))
                .andExpect(jsonPath("$[1].totalSeconds").value(3600));
    }

    @Test
    @DisplayName("queryTopPicks returns top picks summary for a date")
    void queryTopPicks_returnsSummary() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(queryWatcherTopPicksService.query(date)).willReturn(
                Optional.of(new DailyTopPicksSummaryView(
                        subjectUserId, date, 3, 2,
                        List.of(new DailyTopPicksSummaryItemView("공부하기", true, 30, "열심히", 0))
                ))
        );

        mockMvc.perform(get("/accountability-relations/watcher/top-picks")
                        .param("date", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(3))
                .andExpect(jsonPath("$.completedCount").value(2));
    }

    @Test
    @DisplayName("queryTopPicksRange returns top picks summaries for range")
    void queryTopPicksRange_returnsList() throws Exception {
        LocalDate from = LocalDate.of(2026, 4, 18);
        LocalDate to = LocalDate.of(2026, 4, 20);
        given(queryWatcherTopPicksService.queryRange(from, to)).willReturn(
                List.of(
                        new DailyTopPicksSummaryView(subjectUserId, from, 2, 1, List.of()),
                        new DailyTopPicksSummaryView(subjectUserId, to, 3, 3, List.of())
                )
        );

        mockMvc.perform(get("/accountability-relations/watcher/top-picks/range")
                        .param("from", "2026-04-18")
                        .param("to", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].completedCount").value(1))
                .andExpect(jsonPath("$[1].completedCount").value(3));
    }

    @Test
    @DisplayName("queryTimetableTask returns timetable summary for a date")
    void queryTimetableTask_returnsSummary() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(queryWatcherTimetableTaskService.query(date)).willReturn(
                Optional.of(new DailyTimetableSummaryView(
                        subjectUserId, date, 4, 3,
                        List.of(new DailyTimetableSummaryItemView(
                                "수학 공부", true,
                                LocalTime.of(9, 0), LocalTime.of(10, 0),
                                true, 0
                        ))
                ))
        );

        mockMvc.perform(get("/accountability-relations/watcher/timetable-tasks")
                        .param("date", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(4))
                .andExpect(jsonPath("$.completedCount").value(3));
    }

    @Test
    @DisplayName("queryTimetableTaskRange returns timetable summaries for range")
    void queryTimetableTaskRange_returnsList() throws Exception {
        LocalDate from = LocalDate.of(2026, 4, 18);
        LocalDate to = LocalDate.of(2026, 4, 20);
        given(queryWatcherTimetableTaskService.queryRange(from, to)).willReturn(
                List.of(
                        new DailyTimetableSummaryView(subjectUserId, from, 3, 2, List.of()),
                        new DailyTimetableSummaryView(subjectUserId, to, 4, 4, List.of())
                )
        );

        mockMvc.perform(get("/accountability-relations/watcher/timetable-tasks/range")
                        .param("from", "2026-04-18")
                        .param("to", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].totalCount").value(3))
                .andExpect(jsonPath("$[1].completedCount").value(4));
    }
}

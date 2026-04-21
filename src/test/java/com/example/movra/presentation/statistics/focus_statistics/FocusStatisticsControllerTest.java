package com.example.movra.presentation.statistics.focus_statistics;

import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusPeriodStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusTimeOfDayStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusPeriodStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeBucketResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeOfDayStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.presentation.FocusStatisticsController;
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
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FocusStatisticsControllerTest {

    @Mock
    private QueryFocusPeriodStatisticsService queryFocusPeriodStatisticsService;

    @Mock
    private QueryFocusTimeOfDayStatisticsService queryFocusTimeOfDayStatisticsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        FocusStatisticsController controller = new FocusStatisticsController(
                queryFocusPeriodStatisticsService,
                queryFocusTimeOfDayStatisticsService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("queryDaily returns period statistics contract")
    void queryDaily_returnsPeriodStatisticsContract() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 4, 15);
        given(queryFocusPeriodStatisticsService.queryDaily(targetDate)).willReturn(
                FocusPeriodStatisticsResponse.builder()
                        .targetDate(targetDate)
                        .queriedAt(Instant.parse("2026-04-15T03:00:00Z"))
                        .periodStartDate(LocalDate.of(2026, 4, 15))
                        .periodEndDate(LocalDate.of(2026, 4, 15))
                        .dayCount(1)
                        .coveredDayCount(1)
                        .totalFocusSeconds(5400L)
                        .averageDailyFocusSeconds(5400L)
                        .status(FocusStatisticsStatus.FINAL)
                        .dataSource(FocusStatisticsDataSource.SUMMARY)
                        .build()
        );

        mockMvc.perform(get("/focus-statistics/daily")
                        .param("targetDate", "2026-04-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDate").value("2026-04-15"))
                .andExpect(jsonPath("$.coveredDayCount").value(1))
                .andExpect(jsonPath("$.status").value("FINAL"))
                .andExpect(jsonPath("$.dataSource").value("SUMMARY"))
                .andExpect(jsonPath("$.averageDailyFocusSeconds").value(5400));
    }

    @Test
    @DisplayName("queryTimeOfDay returns time-of-day statistics contract")
    void queryTimeOfDay_returnsTimeOfDayStatisticsContract() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 4, 15);
        given(queryFocusTimeOfDayStatisticsService.query(targetDate)).willReturn(
                FocusTimeOfDayStatisticsResponse.builder()
                        .targetDate(targetDate)
                        .queriedAt(Instant.parse("2026-04-15T03:00:00Z"))
                        .totalFocusSeconds(3600L)
                        .status(FocusStatisticsStatus.PARTIAL)
                        .dataSource(FocusStatisticsDataSource.RAW)
                        .hourlyBuckets(List.of(
                                new FocusTimeBucketResponse(9, 1800L),
                                new FocusTimeBucketResponse(10, 1800L)
                        ))
                        .build()
        );

        mockMvc.perform(get("/focus-statistics/time-of-day")
                        .param("targetDate", "2026-04-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDate").value("2026-04-15"))
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.dataSource").value("RAW"))
                .andExpect(jsonPath("$.hourlyBuckets[0].hourOfDay").value(9))
                .andExpect(jsonPath("$.hourlyBuckets[0].focusSeconds").value(1800));
    }
}

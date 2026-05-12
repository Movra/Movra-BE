package com.example.movra.presentation.analytics.activation_event;

import com.example.movra.bc.analytics.activation_event.application.service.QueryAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.RecordAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.dto.response.AnalyticsEventResponse;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_event.presentation.AnalyticsEventController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventControllerTest {

    @Mock
    private RecordAnalyticsEventService recordAnalyticsEventService;

    @Mock
    private QueryAnalyticsEventService queryAnalyticsEventService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(new AnalyticsEventController(
                        recordAnalyticsEventService,
                        queryAnalyticsEventService
                ))
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("record stores analytics event")
    void record_success() throws Exception {
        UUID analyticsEventId = UUID.randomUUID();
        given(recordAnalyticsEventService.record(any())).willReturn(
                AnalyticsEventResponse.builder()
                        .analyticsEventId(analyticsEventId)
                        .eventType(AnalyticsEventType.FOCUS_SESSION_STARTED)
                        .occurredAt(Instant.parse("2026-04-29T01:00:00Z"))
                        .properties(Map.of("entryPoint", "home"))
                        .build()
        );

        mockMvc.perform(post("/analytics/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "FOCUS_SESSION_STARTED",
                                  "properties": {
                                    "entryPoint": "home"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analyticsEventId").value(analyticsEventId.toString()))
                .andExpect(jsonPath("$.eventType").value("FOCUS_SESSION_STARTED"))
                .andExpect(jsonPath("$.properties.entryPoint").value("home"));

        then(recordAnalyticsEventService).should().record(any());
    }

    @Test
    @DisplayName("record returns 400 when event type is omitted")
    void record_missingEventType_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/analytics/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"properties\": {\"entryPoint\": \"home\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("query returns analytics events")
    void query_success() throws Exception {
        UUID analyticsEventId = UUID.randomUUID();
        given(queryAnalyticsEventService.query(
                eq(LocalDate.of(2026, 4, 29)),
                eq(LocalDate.of(2026, 4, 30)),
                eq(AnalyticsEventType.TOP_PICK_SELECTED)
        )).willReturn(List.of(
                AnalyticsEventResponse.builder()
                        .analyticsEventId(analyticsEventId)
                        .eventType(AnalyticsEventType.TOP_PICK_SELECTED)
                        .occurredAt(Instant.parse("2026-04-29T01:00:00Z"))
                        .properties(Map.of("taskId", "task-1"))
                        .build()
        ));

        mockMvc.perform(get("/analytics/events")
                        .param("from", "2026-04-29")
                        .param("to", "2026-04-30")
                        .param("eventType", "TOP_PICK_SELECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analyticsEventId").value(analyticsEventId.toString()))
                .andExpect(jsonPath("$[0].eventType").value("TOP_PICK_SELECTED"))
                .andExpect(jsonPath("$[0].properties.taskId").value("task-1"));
    }
}

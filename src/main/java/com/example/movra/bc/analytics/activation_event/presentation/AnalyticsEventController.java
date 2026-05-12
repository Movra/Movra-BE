package com.example.movra.bc.analytics.activation_event.presentation;

import com.example.movra.bc.analytics.activation_event.application.service.QueryAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.RecordAnalyticsEventService;
import com.example.movra.bc.analytics.activation_event.application.service.dto.request.AnalyticsEventRequest;
import com.example.movra.bc.analytics.activation_event.application.service.dto.response.AnalyticsEventResponse;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics/events")
@RequiredArgsConstructor
public class AnalyticsEventController {

    private final RecordAnalyticsEventService recordAnalyticsEventService;
    private final QueryAnalyticsEventService queryAnalyticsEventService;

    @PostMapping
    public AnalyticsEventResponse record(@Valid @RequestBody AnalyticsEventRequest request) {
        return recordAnalyticsEventService.record(request);
    }

    @GetMapping
    public List<AnalyticsEventResponse> query(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) AnalyticsEventType eventType
    ) {
        return queryAnalyticsEventService.query(from, to, eventType);
    }
}

package com.example.movra.bc.analytics.activation_event.application.service;

import com.example.movra.bc.analytics.activation_event.application.service.dto.request.AnalyticsEventRequest;
import com.example.movra.bc.analytics.activation_event.application.service.dto.response.AnalyticsEventResponse;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordAnalyticsEventService {

    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final CurrentUserQuery currentUserQuery;

    public AnalyticsEventResponse record(AnalyticsEventRequest request) {
        return AnalyticsEventResponse.from(
                analyticsEventRecorder.record(
                        currentUserQuery.currentUser().userId(),
                        request.eventType(),
                        request.properties()
                )
        );
    }
}

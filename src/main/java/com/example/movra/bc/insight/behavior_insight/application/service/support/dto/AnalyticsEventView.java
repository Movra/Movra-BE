package com.example.movra.bc.insight.behavior_insight.application.service.support.dto;

import java.time.Instant;
import java.util.Map;

/**
 * analytics BC의 AnalyticsEvent를 insight 경계 안에서 표현하는 view.
 * eventType은 analytics 도메인 타입을 직접 노출하지 않기 위해 enum name(String)으로 전달한다.
 */
public record AnalyticsEventView(
        String eventType,
        Instant occurredAt,
        Map<String, String> properties
) {}

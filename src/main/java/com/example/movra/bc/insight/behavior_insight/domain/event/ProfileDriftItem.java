package com.example.movra.bc.insight.behavior_insight.domain.event;

import com.example.movra.bc.insight.behavior_insight.domain.type.DriftType;

/**
 * 감지된 괴리 한 건. 표시용 값(declared/observed)과 적용용 제안 값을 함께 담는다.
 * - FOCUS_HOURS: suggestedStartHour/suggestedEndHour 사용.
 * - EXECUTION_DIFFICULTY / RECOVERY_STYLE: suggestedValue(enum name) 사용.
 */
public record ProfileDriftItem(
        DriftType type,
        String declaredValue,
        String observedValue,
        Integer suggestedStartHour,
        Integer suggestedEndHour,
        String suggestedValue,
        String message
) {}

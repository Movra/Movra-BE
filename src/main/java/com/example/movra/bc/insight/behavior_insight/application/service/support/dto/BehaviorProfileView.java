package com.example.movra.bc.insight.behavior_insight.application.service.support.dto;

/**
 * 사용자가 선언한 선호. enum은 personalization 도메인 타입을 직접 노출하지 않도록 name(String)으로 전달한다.
 * Phase 2 LLM 톤 제어 및 Phase 4 프로필 괴리 감지의 컨텍스트로 사용한다.
 */
public record BehaviorProfileView(
        int preferredFocusStartHour,
        int preferredFocusEndHour,
        String coachingMode,
        String recoveryStyle,
        String executionDifficulty
) {}

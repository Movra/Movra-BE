package com.example.movra.bc.insight.behavior_insight.application.service.support.dto;

/**
 * 회고 정성 텍스트. Phase 2 LLM 서사 생성의 입력으로 사용한다.
 */
public record ReflectionTextView(
        String whatWentWell,
        String whatBrokeDown
) {}

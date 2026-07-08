package com.example.movra.bc.insight.behavior_insight.application.service.llm.dto;

import java.util.List;

/**
 * LLM 구조화 출력 대상. Spring AI ChatClient의 .entity()로 이 record에 직접 매핑된다.
 * profileSuggestion은 프로필 조정 제안이 없으면 빈 문자열일 수 있다.
 */
public record InsightNarrativeContent(
        String summary,
        List<String> strengths,
        List<String> improvements,
        List<String> patterns,
        String motivation,
        String profileSuggestion
) {}

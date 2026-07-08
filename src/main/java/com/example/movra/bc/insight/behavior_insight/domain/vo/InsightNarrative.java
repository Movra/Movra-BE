package com.example.movra.bc.insight.behavior_insight.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LLM이 생성한 서사·동기부여 결과. 리스트형 항목은 줄바꿈으로 join해 TEXT 컬럼에 저장하고,
 * 조회 시 다시 List로 분리한다. 도메인은 application 레이어의 LLM DTO에 의존하지 않는다.
 */
@Embeddable
public record InsightNarrative(
        @Column(name = "narrative_summary", columnDefinition = "TEXT")
        String summary,

        @Column(name = "narrative_strengths", columnDefinition = "TEXT")
        String strengths,

        @Column(name = "narrative_improvements", columnDefinition = "TEXT")
        String improvements,

        @Column(name = "narrative_patterns", columnDefinition = "TEXT")
        String patterns,

        @Column(name = "narrative_motivation", columnDefinition = "TEXT")
        String motivation,

        @Column(name = "narrative_profile_suggestion", columnDefinition = "TEXT")
        String profileSuggestion
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String DELIMITER = "\n";

    public static InsightNarrative of(
            String summary,
            List<String> strengths,
            List<String> improvements,
            List<String> patterns,
            String motivation,
            String profileSuggestion
    ) {
        return new InsightNarrative(
                summary,
                join(strengths),
                join(improvements),
                join(patterns),
                motivation,
                profileSuggestion
        );
    }

    public List<String> strengthsAsList() {
        return split(strengths);
    }

    public List<String> improvementsAsList() {
        return split(improvements);
    }

    public List<String> patternsAsList() {
        return split(patterns);
    }

    private static String join(List<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        String joined = items.stream()
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.joining(DELIMITER));
        return joined.isBlank() ? null : joined;
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(DELIMITER))
                .filter(item -> !item.isBlank())
                .toList();
    }
}

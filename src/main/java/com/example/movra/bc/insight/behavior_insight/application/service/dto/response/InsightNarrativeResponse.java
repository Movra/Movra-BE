package com.example.movra.bc.insight.behavior_insight.application.service.dto.response;

import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightNarrative;

import java.util.List;

public record InsightNarrativeResponse(
        String summary,
        List<String> strengths,
        List<String> improvements,
        List<String> patterns,
        String motivation,
        String profileSuggestion
) {

    public static InsightNarrativeResponse from(InsightNarrative narrative) {
        if (narrative == null) {
            return null;
        }
        return new InsightNarrativeResponse(
                narrative.summary(),
                narrative.strengthsAsList(),
                narrative.improvementsAsList(),
                narrative.patternsAsList(),
                narrative.motivation(),
                narrative.profileSuggestion()
        );
    }
}

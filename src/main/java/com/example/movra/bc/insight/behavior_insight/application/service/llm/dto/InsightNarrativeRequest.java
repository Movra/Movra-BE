package com.example.movra.bc.insight.behavior_insight.application.service.llm.dto;

import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.ReflectionTextView;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;

import java.util.List;

/**
 * 서사 생성에 필요한 입력 묶음. profile은 없을 수 있다(null 허용).
 * driftMessages는 ProfileDriftDetector가 코드로 확정한 괴리 근거로, LLM이 profileSuggestion을 이에 기반해 작성한다.
 */
public record InsightNarrativeRequest(
        AnalysisPeriod period,
        InsightMetrics metrics,
        List<ReflectionTextView> reflections,
        BehaviorProfileView profile,
        List<String> driftMessages
) {}

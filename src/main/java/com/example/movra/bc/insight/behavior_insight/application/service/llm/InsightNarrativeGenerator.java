package com.example.movra.bc.insight.behavior_insight.application.service.llm;

import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeContent;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeRequest;

/**
 * 검증된 지표 + 정성 회고를 받아 인사이트·동기부여 서사를 생성하는 포트.
 * 구현체는 외부 LLM(OpenAI 등)에 의존하며, 테스트에서는 가짜 구현으로 대체한다.
 */
public interface InsightNarrativeGenerator {

    InsightNarrativeContent generate(InsightNarrativeRequest request);
}

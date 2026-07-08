package com.example.movra.bc.insight.behavior_insight.application.service.llm;

import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeContent;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeRequest;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.ReflectionTextView;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OpenAI 기반 서사 생성 구현체. 결정론적으로 계산된 지표와 정성 회고를 입력으로 받아
 * 인사이트·동기부여 서사를 생성한다(원시 데이터 분석은 시키지 않는다).
 * 모델·온도는 application.yml의 spring.ai.openai 설정을 따른다.
 */
@Component
public class OpenAiInsightNarrativeGenerator implements InsightNarrativeGenerator {

    private static final int MAX_REFLECTIONS = 20;

    private final ChatClient chatClient;

    public OpenAiInsightNarrativeGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public InsightNarrativeContent generate(InsightNarrativeRequest request) {
        return chatClient.prompt()
                .system(systemPrompt(request.profile()))
                .user(userPrompt(request))
                .call()
                .entity(InsightNarrativeContent.class);
    }

    private String systemPrompt(BehaviorProfileView profile) {
        String tone = toneInstruction(profile);
        return """
                당신은 사용자의 일정·집중 행동을 분석해 주는 한국어 행동 분석 코치입니다.
                반드시 한국어로 응답하세요.
                아래 규칙을 지키세요:
                - 제공된 지표(숫자)만 근거로 삼고, 수치를 새로 지어내지 마세요.
                - 회고 텍스트는 맥락 해석에만 사용하세요.
                - %s 어조로 작성하세요.
                - summary는 한 달을 요약하는 한두 문장.
                - strengths/improvements/patterns는 각각 2~4개의 간결한 항목.
                - motivation은 사용자를 다음 달에도 움직이게 하는 동기부여 메시지.
                - profileSuggestion은 선언한 선호와 실제 행동의 괴리가 보이면 조정 제안을, 없으면 빈 문자열을 넣으세요.
                """.formatted(tone);
    }

    private String toneInstruction(BehaviorProfileView profile) {
        if (profile == null || profile.coachingMode() == null) {
            return "균형 잡히고 객관적인";
        }
        return switch (profile.coachingMode()) {
            case "GENTLE" -> "따뜻하고 격려하는";
            case "STRICT" -> "직설적이고 도전적인";
            default -> "균형 잡히고 객관적인";
        };
    }

    private String userPrompt(InsightNarrativeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("[분석 기간]\n").append(periodText(request.period())).append("\n\n");
        sb.append("[행동 지표]\n").append(metricsText(request.metrics())).append("\n");
        sb.append(profileText(request.profile())).append("\n");
        sb.append(driftText(request.driftMessages())).append("\n");
        sb.append("[회고 발췌]\n").append(reflectionsText(request.reflections()));
        return sb.toString();
    }

    private String driftText(List<String> driftMessages) {
        if (driftMessages == null || driftMessages.isEmpty()) {
            return "\n[감지된 괴리]\n- 없음";
        }
        StringBuilder sb = new StringBuilder("\n[감지된 괴리] (이 항목들을 근거로 profileSuggestion을 작성하세요)\n");
        driftMessages.forEach(message -> sb.append("- ").append(message).append("\n"));
        return sb.toString().trim();
    }

    private String periodText(AnalysisPeriod period) {
        return "%s ~ %s (%d일)".formatted(period.periodStart(), period.periodEnd(), period.days());
    }

    private String metricsText(InsightMetrics m) {
        String peak = m.peakFocusHour() == null ? "데이터 없음" : m.peakFocusHour() + "시대";
        return """
                - 총 집중 시간(초): %d
                - 완료한 집중 세션 수: %d
                - 중단/자동마감 세션 수: %d
                - 집중 완료율: %.2f
                - 가장 집중이 많은 시간대: %s
                - 활동한 날 수: %d
                - Top Pick 선정 횟수: %d
                - 회고 작성 수: %d
                - 작은 성취(TinyWin) 수: %d"""
                .formatted(
                        m.totalFocusSeconds(),
                        m.completedSessionCount(),
                        m.abandonedSessionCount(),
                        m.focusCompletionRate(),
                        peak,
                        m.activeDayCount(),
                        m.topPickSelectedCount(),
                        m.reflectionCount(),
                        m.tinyWinCount()
                );
    }

    private String profileText(BehaviorProfileView profile) {
        if (profile == null) {
            return "\n[선언한 선호]\n- 없음";
        }
        return """

                [선언한 선호]
                - 선호 집중 시간대: %d시 ~ %d시
                - 코칭 모드: %s
                - 회복 스타일: %s
                - 실행 난이도: %s"""
                .formatted(
                        profile.preferredFocusStartHour(),
                        profile.preferredFocusEndHour(),
                        profile.coachingMode(),
                        profile.recoveryStyle(),
                        profile.executionDifficulty()
                );
    }

    private String reflectionsText(List<ReflectionTextView> reflections) {
        if (reflections == null || reflections.isEmpty()) {
            return "- 없음";
        }
        StringBuilder sb = new StringBuilder();
        reflections.stream().limit(MAX_REFLECTIONS).forEach(reflection -> sb
                .append("- 잘된 점: ").append(safe(reflection.whatWentWell()))
                .append(" / 무너진 점: ").append(safe(reflection.whatBrokeDown()))
                .append("\n"));
        return sb.toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

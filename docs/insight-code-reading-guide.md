# 분석 AI(insight) 코드 리딩 가이드

> 직접 읽고 수정하기 위한 안내서. "어디부터 읽고 → 무엇을 바꾸려면 어디를 건드리는지"에 초점을 둔다.

## 1. 큰 그림 (30초 요약)

한 사용자의 최근 30일 행동을 모아 → **숫자(지표)를 코드로 계산**하고 → 선언한 선호와의 **괴리를 감지**하고 → **LLM이 그 숫자를 해석해 서사·동기부여를 생성**하고 → 저장 후 **푸시로 알린다.** 사용자가 알림을 열면 리포트를 조회하고, 괴리 제안을 수락하면 프로필이 바뀐다.

이 모든 흐름의 **중심은 단 하나의 메서드**다:

```
GenerateInsightReportService.generate(userId, period)
```

이 메서드 하나가 나머지 모든 조각을 순서대로 호출한다. **여기부터 읽으면 전체가 보인다.**

## 2. 레이어와 의존 규칙 (DDD)

insight BC는 세 겹이다. 의존은 항상 **안쪽(domain) ← 바깥쪽** 한 방향이다.

```
presentation (HTTP)  →  application (흐름·유스케이스)  →  domain (규칙·데이터)
```

- **domain**: 데이터와 규칙. 다른 레이어를 모른다. (`InsightReport`, VO들, 이벤트)
- **application**: 유스케이스 조립. 다른 BC는 **직접 참조하지 않고 "포트(인터페이스)"로만** 접근한다.
- **presentation**: HTTP 입출력만. 얇다.

핵심 원칙 두 가지만 기억하면 된다:
1. **다른 BC의 데이터는 read-port(인터페이스)로만 가져온다** — insight는 analytics/focus/feedback/personalization의 *내부*를 모른다.
2. **지표는 코드로, 서사는 LLM으로** — 숫자 계산과 LLM 해석을 분리했다.

## 3. 읽는 순서 (이대로 따라가면 됨)

**① 흐름의 척추 — 먼저 이걸 정독**
`application/service/GenerateInsightReportService.java`
→ generate() 안을 위에서 아래로 읽으면 "데이터 수집 → 지표 → 괴리 → LLM → 저장 → 이벤트" 순서가 그대로 보인다. 나머지 파일은 전부 여기서 호출되는 부품이다.

**② 무엇이 저장되는가 — 도메인**
`domain/InsightReport.java` 와 `domain/vo/` 의 네 VO
- `AnalysisPeriod` (분석 기간), `InsightMetrics` (계산된 숫자들), `InsightNarrative` (LLM 결과), `InsightReportId`
→ 리포트 한 건이 어떤 모양인지 파악.

**③ 입력은 어디서 오나 — read-port와 어댑터**
`application/service/support/` 의 `*ReadPort.java`(인터페이스)와 `*ReadAdapter.java`(구현)
→ 4개 BC의 데이터를 어떻게 경계 안 깨고 가져오는지. 포트는 "무엇이 필요한지", 어댑터는 "실제로 어디서 가져오는지".

**④ 숫자는 어떻게 나오나 — 계산기**
`application/service/support/InsightMetricsCalculator.java`
→ 완료율·피크 시간대·재개율 등 모든 지표의 계산 로직. LLM 없음. 순수 함수라 테스트도 쉽다(`InsightMetricsCalculatorTest`).

**⑤ 괴리는 어떻게 잡나 — 감지기**
`application/service/support/ProfileDriftDetector.java`
→ 선언 vs 실제 비교. 임계값(상수)과 세 가지 detect 메서드.

**⑥ LLM은 어떻게 부르나**
`application/service/llm/OpenAiInsightNarrativeGenerator.java`
→ 프롬프트 조립 + Spring AI `ChatClient.entity()` 호출. 톤·프롬프트가 여기 다 있다.

**⑦ 어떻게 실행되나 — 두 진입점**
- 자동: `application/schedule/InsightGenerationScheduler.java` → `InsightSchedulingService` → `InsightDueResolver`
- 수동(admin): `application/service/TriggerInsightGenerationService.java`

**⑧ 끝난 뒤 — 이벤트와 알림**
`application/event/InsightReportNotifier.java` (FCM 푸시), `presentation/InsightReportController.java` (조회 API)

**⑨ 피드백 루프의 반대편 — personalization**
`bc/personalization/behavior_profile/application/event/ProfileDriftDetectedEventHandler.java` 와 `application/service/AcceptProfileAdjustmentService.java`
→ 괴리 이벤트를 받아 제안으로 저장하고, 수락 시 프로필에 반영하는 부분.

## 4. 핵심 파일 지도

| 파일 | 역할 |
|------|------|
| `GenerateInsightReportService` | **척추.** 전체 흐름 조립 |
| `InsightReport` (+ vo 4개) | 리포트 애그리거트와 값 객체 |
| `*ReadPort` / `*ReadAdapter` | 다른 BC 데이터 수집(ACL) |
| `InsightMetricsCalculator` | 결정론적 지표 계산 |
| `ProfileDriftDetector` | 선언 vs 실제 괴리 감지 |
| `InsightNarrativeGenerator` (포트) / `OpenAiInsightNarrativeGenerator` (구현) | LLM 서사 생성 |
| `InsightSchedulingService` / `InsightDueResolver` | 롤링 30일 자동 실행·due 판정 |
| `TriggerInsightGenerationService` | admin 수동 트리거 |
| `InsightReportNotifier` | 완료 시 FCM 푸시 |
| `InsightReportController` | 조회·생성 HTTP API |
| `ProfileDriftDetectedEventHandler` | (personalization) 괴리 → 조정 제안 저장 |
| `AcceptProfileAdjustmentService` | (personalization) 수락 시 프로필 반영 |

## 5. "이걸 바꾸고 싶으면 여기" (수정 지도)

| 바꾸고 싶은 것 | 건드릴 곳 |
|----------------|-----------|
| LLM 모델 (gpt-4.1-mini → 다른 모델) | `application.yml` → `spring.ai.openai.chat.options.model` |
| LLM 말투/프롬프트 내용 | `OpenAiInsightNarrativeGenerator`의 `systemPrompt()` / `userPrompt()` |
| 새 지표 추가 / 계산식 변경 | `InsightMetricsCalculator` (+ `InsightMetrics` VO에 필드 추가) |
| 괴리 판정 임계값 | `ProfileDriftDetector`의 상수들 (예: `LOW_COMPLETION`) |
| 분석 주기 (30일 → N일) | `InsightDueResolver.WINDOW_DAYS` |
| 자동 실행 시각 | `InsightGenerationScheduler`의 cron 기본값 |
| admin 계정 | `TriggerInsightGenerationService.ADMIN_ACCOUNT_ID` |
| 푸시 문구 | `InsightReportNotifier`의 `TITLE` / `BODY` |
| 응답에 나가는 필드 | `application/service/dto/response/InsightReportResponse` |

## 6. 두 가지 실행 경로 (헷갈리지 말 것)

- **자동(운영)**: 매일 8시 스케줄러 → 활동 사용자 중 30일 윈도우가 도래한 사람만 생성.
- **수동(테스트/검증)**: admin이 `POST /insights/generate` 호출 → 본인 데이터로 즉시 생성.

둘 다 결국 같은 `GenerateInsightReportService.generate()`를 호출한다. 차이는 "언제·누구를·어떤 기간으로" 부르느냐일 뿐이다.

## 7. 이벤트 흐름 (느슨한 결합의 핵심)

`generate()`가 끝나면 두 이벤트가 나간다. 둘 다 **커밋 이후(AFTER_COMMIT)** 동작해, 리포트가 실제 저장된 경우에만 후속 작업이 일어난다.

1. `InsightReportGeneratedEvent` → `InsightReportNotifier` → **FCM 푸시**
2. `ProfileDriftDetectedEvent` → (personalization) `ProfileDriftDetectedEventHandler` → **조정 제안 저장**

이벤트 덕분에 insight는 "누가 받는지" 모르고 발행만 한다. 푸시 로직이나 제안 저장 로직을 바꿔도 `generate()`는 안 건드려도 된다.

## 8. 직접 해보기 좋은 첫 수정 (연습 추천)

1. **프롬프트 톤 한 줄 바꾸기** — `OpenAiInsightNarrativeGenerator.systemPrompt()`에서 문구를 고치고 `POST /insights/generate`로 결과 비교.
2. **괴리 임계값 조정** — `ProfileDriftDetector`의 `LOW_COMPLETION`을 0.4 → 0.5로 바꾸고, 같은 시드 데이터로 제안이 어떻게 달라지는지 관찰.
3. **지표 하나 추가** — `InsightMetricsCalculator`에 간단한 지표(예: 일평균 집중시간)를 더하고 `InsightMetrics`·응답 DTO에 노출.

작은 것부터 바꿔 보고 `POST /insights/generate`로 즉시 확인하는 게 가장 빠른 학습 루프다.

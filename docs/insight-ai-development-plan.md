# 사용자 행동 분석 AI(insight BC) 개발 계획

> 30일 주기로 사용자 행동 데이터를 수집·분석하여 인사이트와 동기부여를 제공하는 AI 기능.
> 기존 4개 BC(analytics, statistics, feedback, personalization)를 조합하는 **오케스트레이션 계층**을 신규 `insight` BC로 구현한다.

## 0. 확정된 의사결정

| 항목 | 결정 |
|------|------|
| v1 범위 | **전체 파이프라인** (지표 계산 + LLM 서사 + 프로필 피드백 루프) |
| 분석 주기 | **가입일 기준 롤링 30일** (사용자별 앵커) |
| LLM 프레임워크 | **Spring AI 1.1.x** (Spring Boot 3.5 호환. 2.0은 Boot 4 필요 → 미사용) |
| LLM 프로바이더 | **보유 API 키 기준** (⚠️ 미확정 — 아래 9번 참조) |
| 결과 전달 | **API 조회 + FCM 푸시 알림** (기존 notification BC 활용) |

## 목차

1. [목표와 설계 원칙](#1-목표와-설계-원칙)
2. [신규 insight BC 패키지 구조](#2-신규-insight-bc-패키지-구조)
3. [도메인 모델: InsightReport](#3-도메인-모델-insightreport)
4. [데이터 수집: read-port(ACL)](#4-데이터-수집-read-portacl)
5. [지표 계산(결정론적)](#5-지표-계산결정론적)
6. [LLM 연동: 서사·동기부여 생성](#6-llm-연동서사동기부여-생성)
7. [프로필 피드백 루프](#7-프로필-피드백-루프)
8. [스케줄링·전달·API](#8-스케줄링전달api)
9. [열린 결정 사항](#9-열린-결정-사항)
10. [단계별 마일스톤](#10-단계별-마일스톤)
11. [테스트 전략](#11-테스트-전략)
12. [리스크](#12-리스크)

---

## 1. 목표와 설계 원칙

**목표.** 사용자의 30일치 행동을 분석해 (1) 객관적 패턴 인사이트와 (2) 동기부여 메시지를 제공하고, (3) 선언된 선호(BehaviorProfile)와 실제 행동의 괴리를 감지해 맞춤 조정을 제안한다.

**4대 원칙.**

1. **경계 보존** — `insight` BC는 다른 BC의 도메인 내부(애그리거트·엔티티)를 직접 참조하지 않는다. 각 BC가 공개한 조회 인터페이스(read-port)에만 의존한다. 의존 방향은 항상 `insight → 추상 포트` 단방향.
2. **지표/서사 분리** — *숫자*는 코드로 결정론적으로 계산(테스트·재현 가능, LLM 비용 0). LLM은 검증된 지표 + 정성 회고를 **해석·격려**만 한다.
3. **부분 실패 허용** — LLM 서사 생성이 실패해도 계산된 지표는 영속화한다. 서사는 별도로 재시도.
4. **컨벤션 준수** — read-port는 `application/service/support/`, 스케줄러는 `application/schedule/` + cron 설정값, 크로스 BC는 도메인 이벤트, 애그리거트는 `create()` 팩토리. 기존 코드와 동일.

---

## 2. 신규 insight BC 패키지 구조

```
src/main/java/com/example/movra/bc/insight/behavior_insight/
├── domain/
│   ├── InsightReport.java                 # 애그리거트 루트
│   ├── event/
│   │   └── InsightReportGeneratedEvent.java
│   ├── vo/
│   │   ├── InsightReportId.java
│   │   ├── AnalysisPeriod.java            # periodStart ~ periodEnd (롤링 30일)
│   │   ├── InsightMetrics.java            # @Embedded 결정론 지표 묶음
│   │   └── InsightNarrative.java          # @Embedded LLM 산출 서사
│   ├── type/
│   │   └── InsightReportStatus.java       # METRICS_READY, COMPLETED, NARRATIVE_FAILED
│   ├── exception/
│   └── repository/
│       └── InsightReportRepository.java
├── application/
│   ├── service/
│   │   ├── GenerateInsightReportService.java   # 오케스트레이터(얇게)
│   │   ├── QueryInsightReportService.java
│   │   ├── support/                            # read-port(ACL) + 계산기
│   │   │   ├── AnalyticsEventReadPort.java
│   │   │   ├── FocusStatisticsReadPort.java    # 기존 포트 재사용/위임
│   │   │   ├── ReflectionReadPort.java
│   │   │   ├── BehaviorProfileReadPort.java
│   │   │   ├── InsightMetricsCalculator.java
│   │   │   ├── ProfileDriftDetector.java
│   │   │   └── dto/                            # 포트 반환 view record
│   │   └── llm/
│   │       ├── InsightNarrativeGenerator.java  # 포트(인터페이스)
│   │       └── dto/InsightNarrativeContent.java # 구조화 출력 record
│   ├── schedule/
│   │   └── InsightGenerationScheduler.java
│   ├── event/
│   │   └── InsightReportGeneratedEventHandler.java  # → FCM 푸시
│   └── exception/
└── presentation/
    └── InsightReportController.java

src/main/java/com/example/movra/bc/insight/.../infrastructure/   # 각 source BC 쪽 어댑터
```

> 어댑터(read-port 구현체)는 각 source BC가 자기 쿼리 서비스로 구현해 노출하거나, `insight` 측 infrastructure에 ACL 어댑터로 둔다. 기존 `FocusStatisticsReadPort` 위임 방식을 그대로 확장.

---

## 3. 도메인 모델: InsightReport

사용자×기간 단위 분석 스냅샷. 30일 윈도우라 "Monthly"가 아니라 명시적 기간(periodStart/End)을 갖는다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `insightReportId` | `InsightReportId` (@EmbeddedId) | PK |
| `userId` | `UserId` (@Embedded) | 대상 사용자 |
| `period` | `AnalysisPeriod` (@Embedded) | periodStart, periodEnd (롤링 30일) |
| `metrics` | `InsightMetrics` (@Embedded) | 결정론적 지표 묶음 |
| `narrative` | `InsightNarrative` (@Embedded, nullable) | LLM 산출 (summary/strengths/improvements/motivation) |
| `status` | `InsightReportStatus` | METRICS_READY → COMPLETED / NARRATIVE_FAILED |
| `generatedAt` | `Instant` | 생성 시각 |

- 제약: `@UniqueConstraint(user_id, period_start)` — 한 윈도우당 1리포트.
- `AbstractAggregateRoot` 상속, `InsightReport.create(...)` 팩토리.
- 서사 생성 성공 시 `markNarrativeCompleted(narrative)`, 실패 시 `markNarrativeFailed()` → `registerEvent(InsightReportGeneratedEvent)`로 푸시 트리거.

---

## 4. 데이터 수집: read-port(ACL)

`insight` BC 안에 4개 포트를 정의하고, 각 source BC가 구현한다. 반환 타입은 `insight` 전용 view record(원본 도메인 노출 금지).

| 포트 | 출처 BC | 제공 데이터 |
|------|---------|-------------|
| `AnalyticsEventReadPort` | analytics | 기간 내 `AnalyticsEvent` 스트림 (event_type, occurredAt, properties) — **백본 입력** |
| `FocusStatisticsReadPort` | statistics | 기존 포트 재사용: 기간별 집중 요약, 세션 view, 시간대 분포 |
| `ReflectionReadPort` | feedback | 기간 내 DailyReflection 텍스트, TinyWin 개수/내용 |
| `BehaviorProfileReadPort` | personalization | 선언된 선호(난이도·회복스타일·선호집중시간대·코칭모드) — 컨텍스트 |

모두 `findXxx(UserId, Instant from, Instant to)` 시그니처. 오케스트레이터는 이 4개 포트만 알고 구현체는 모른다.

---

## 5. 지표 계산(결정론적)

`InsightMetricsCalculator`가 포트 데이터로 LLM 없이 계산. 후보 지표:

**집중(focus/statistics 기반)**
- 총 집중 시간, 일평균, 완료 세션 수
- 완료율 = 완료 / (완료+포기+자동마감)
- 시간대 분포 (실제 집중 피크 시간대)
- 전월 동기간 대비 증감

**계획 실행(analytics 이벤트 기반)**
- top pick 선정 → 완료 달성률
- morning task 생성 빈도, 타임테이블 슬롯 생성 수
- 활동 일수 / 30일 (꾸준함)

**정성/회복(feedback 기반)**
- DailyReflection 작성률, TinyWin 누적
- RECOVERY_CARD_VIEWED / ACTIONED 비율

**괴리 신호(personalization 대조)**
- 선언 선호집중시간대 vs 실제 집중 피크 시간대 차이
- 선언 회복스타일 vs 실제 포기 후 복귀 패턴

> 지표는 모두 `InsightMetrics` VO로 묶어 영속화 → status `METRICS_READY`.

---

## 6. LLM 연동: 서사·동기부여 생성

**프레임워크.** Spring AI 1.1.x. 의존성: `spring-ai-<provider>-spring-boot-starter` (프로바이더 확정 후).

**포트 추상화.** `InsightNarrativeGenerator` 인터페이스 뒤에 Spring AI 구현체를 둔다. 테스트는 가짜 구현으로 실제 호출 없이 검증.

**구조화 출력.** Spring AI `ChatClient` + `.entity(InsightNarrativeContent.class)`로 record 직접 매핑:

```java
public record InsightNarrativeContent(
    String summary,                 // 30일 한 줄 총평
    List<String> strengths,         // 잘한 점
    List<String> improvements,      // 개선 제안
    List<String> patterns,          // 발견된 행동 패턴
    String motivation,              // 동기부여 메시지(코칭톤 반영)
    String profileSuggestion        // 프로필 조정 제안(없으면 빈 문자열)
) {}
```

**프롬프트 구조.**
- system: 역할(행동 분석 코치), 한국어, **`BehaviorProfile.coachingMode`에 맞춘 어조**(예: GENTLE vs STRICT), "주어진 지표만 근거로 삼고 수치를 지어내지 말 것".
- user: 계산된 `InsightMetrics`(JSON) + 회고 텍스트 요약.

**실패 처리.** 호출 실패/검증 실패 시 status `NARRATIVE_FAILED`로 두고 metrics는 보존. 별도 재시도 잡 또는 다음 조회 시 lazy 재생성.

---

## 7. 프로필 피드백 루프 (v1 포함)

`ProfileDriftDetector`가 5번의 괴리 신호를 임계값과 비교해 드리프트를 판정한다. 드리프트가 있으면:

1. `InsightNarrativeContent.profileSuggestion`에 자연어 제안 포함 (사용자 노출).
2. `insight` BC가 도메인 이벤트(`ProfileDriftDetectedEvent`)를 발행 → personalization BC가 `@TransactionalEventListener(BEFORE_COMMIT)`로 수신해 "조정 제안" 상태를 기록(자동 변경은 하지 않고 사용자 승인 유도).

> 자동 덮어쓰기 금지 — 사용자가 선언한 값은 사용자 동의로만 변경. 분석은 "제안"까지만.

---

## 8. 스케줄링·전달·API

**롤링 30일 스케줄링.** 캘린더 월과 달리 사용자마다 앵커일이 다르므로:
- `InsightGenerationScheduler`가 **매일** 1회 실행(예: `0 30 0 * * *`, Asia/Seoul).
- "다음 분석 예정일 ≤ 오늘"인 사용자를 조회해 일괄 생성.
- 예정일 = `lastReportPeriodEnd + 1일` (최초는 `가입일 + 30일`). 마지막 리포트 기준으로 다음 윈도우를 계산해 누락·중복 방지.
- 사용자 수 증가 시 배치/페이징 처리. (기존 `DailyTimetableClosingScheduler` 패턴 확장)

**FCM 푸시 전달.** `InsightReportGeneratedEvent` → `InsightReportGeneratedEventHandler` → 기존 notification BC를 통해 "이번 달 리포트가 도착했어요" 푸시 발송.

**API.** `InsightReportController` (presentation)
- `GET /insights/latest` — 최신 리포트 조회
- `GET /insights/{period}` — 특정 기간 리포트
- (선택) `POST /insights/{id}/profile-suggestion/accept` — 프로필 조정 제안 수락

**프라이버시.** 회고·행동 데이터를 외부 LLM에 전송 → 동의/PII 처리 지점 필요. v1에서는 최소한 (a) 전송 전 식별자 제거, (b) 약관/동의 플래그 확인을 설계에 포함.

---

## 9. 열린 결정 사항

| # | 항목 | 필요 입력 |
|---|------|-----------|
| 1 | **LLM 프로바이더** | 보유 중인 API 키(Anthropic/Google/OpenAI) → 의존성·모델명·설정 확정 |
| 2 | 모델 등급 | 지표 우선 설계라 mid/small급 권장. 실데이터로 2~3종 소규모 평가 후 확정 |
| 3 | 괴리 임계값 | 시간대 차이 N시간, 완료율 변화 N%p 등 구체 수치 |
| 4 | 푸시 문구·발송 시각 | 사용자 앵커일 0시 직후 vs 오전 고정 시각 |
| 5 | 신규 BC 위치 | `bc/insight/behavior_insight` 명칭 확정 |

---

## 10. 단계별 마일스톤

전체 파이프라인을 한 번에 머지하지 않고 검증 가능한 단위로 분할.

**Phase 1 — 골격 + 지표**
- insight BC 패키지, `InsightReport` 애그리거트, repository
- 4개 read-port 인터페이스 + 어댑터
- `InsightMetricsCalculator` + 단위 테스트 (LLM 없음)
- 산출물: metrics만 채운 리포트 영속화

**Phase 2 — LLM 서사**
- Spring AI 의존성, `InsightNarrativeGenerator` 포트 + 구현체
- 구조화 출력 record, 프롬프트(코칭톤)
- 부분 실패 처리, 가짜 구현 기반 테스트

**Phase 3 — 스케줄링 + 전달**
- 롤링 30일 스케줄러, FCM 푸시 핸들러
- `InsightReportController` API

**Phase 4 — 프로필 피드백 루프**
- `ProfileDriftDetector`, `ProfileDriftDetectedEvent`
- personalization 수신 핸들러(제안 기록), 수락 API

**Phase 5 — 마감**
- ArchUnit 의존 규칙(아래), 통합 테스트, 프라이버시 점검

---

## 11. 테스트 전략

기존 컨벤션(application 레이어 테스트 집중, `methodName_condition_expectedResult`) 준수.

- `InsightMetricsCalculator` — 포트 fake로 입력 주입, 지표 수치 검증(결정론적이라 정밀 검증 가능).
- `GenerateInsightReportService` — 4개 포트 + `InsightNarrativeGenerator` 모두 fake. LLM 실호출 없음.
- 부분 실패 — 서사 generator가 예외 던질 때 metrics 보존 + status `NARRATIVE_FAILED` 검증.
- `ProfileDriftDetector` — 경계값 테스트.
- **ArchUnit 규칙 신설**(프로젝트에 archunit 의존성은 있으나 미사용): `insight.domain`이 application/presentation을 참조 금지, `insight`가 다른 BC의 `domain` 패키지 직접 참조 금지(포트만 허용).

---

## 12. 리스크

| 리스크 | 대응 |
|--------|------|
| analytics 이벤트 커버리지 부족 → 빈약한 분석 | Phase 1 전 이벤트/properties 감사. 빠진 이벤트 보강 |
| 30일 데이터 부족(신규 유저) | 최소 활동 임계 미달 시 리포트 생성 보류 또는 간소 버전 |
| LLM 비용/지연 | 지표 우선 설계로 토큰 최소화, mid/small 모델, 월 1회 호출 |
| 외부 LLM 데이터 전송(PII) | 식별자 제거 + 동의 확인 |
| 롤링 스케줄 누락/중복 | lastReportPeriodEnd 기준 계산 + 유니크 제약 |

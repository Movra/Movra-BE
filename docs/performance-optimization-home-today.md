# 홈 조회 API 성능 측정·최적화 기록 — `GET /home/today`

> DAU 150 / Peak 10 TPS / 조회 중심이라는 목표를 전제로 `GET /home/today`를
> **측정·프로파일링 데이터**로 점검한 기록. 추측으로 고치지 않고, 측정이 가리키는
> 것만 손댔다. 효과가 없었던 변경도 그대로 기록한다.

## 목차

- [배경](#배경)
- [측정 전제](#측정-전제)
- [측정 기록](#측정-기록)
- [프로파일링 — 56ms는 어디로 가는가](#프로파일링--56ms는-어디로-가는가)
- [적용한 변경](#적용한-변경)
- [결론](#결론)
- [측정의 한계](#측정의-한계)
- [변경 파일 인덱스](#변경-파일-인덱스)

---

## 배경

이 엔드포인트에는 Redis 캐시(미래비전·알림설정·다음시험 3종)가 적용돼 있었다.
목표 규모(DAU 150, Peak 10 TPS, 조회 중심)에 비추어 캐시가 실제로 필요한지를
측정으로 재검증하기로 하고, 캐시를 걷어낸 상태에서 베이스라인부터 다시 쟀다.

---

## 측정 전제

| 항목 | 값 |
| --- | --- |
| 목표 부하 | DAU 150, 피크 10 TPS, 조회 중심 서비스 |
| 측정 도구 | Gatling 3.13.5 |
| 측정 환경 | 로컬 단일 머신 (앱·MySQL·Redis·Gatling 동거), Spring Boot 3.5, Java 21, Virtual Threads on |
| 시뮬레이션 | `HomeTodayBaselineSimulation` (1유저 순차), `HomeTodayPeakLoadSimulation` (10 TPS 지속) |
| 측정 계정 | 단일 테스트 계정 (홈 데이터 보유) |

`HomeTodayPeakLoadSimulation`은 목표 부하를 모델링한다 — warmup 2 req/s(10s) →
1→10 req/s 램프(30s) → 10 req/s 지속(120s), 총 1,385건. 표본이 커서
1유저 순차(n=33)보다 통계적으로 신뢰도가 높다.

---

## 측정 기록

3차에 걸쳐 측정했다. **각 차수의 조건을 명시한다 — 조건을 섞으면 무엇이 효과를
냈는지 알 수 없기 때문이다.** (이전 문서가 캐시와 N+1 개선을 한 수치에 섞어
제시했던 것을 반복하지 않는다.)

### 10 TPS 지속 (`HomeTodayPeakLoadSimulation`, 1,385건, 실패율 0%)

| 지표 | Run 1 | Run 2 | Run 3 |
| --- | ---: | ---: | ---: |
| mean | 77 ms | 56 ms | 58 ms |
| p50 | 75 ms | 54 ms | 56 ms |
| p75 | 85 ms | 60 ms | 62 ms |
| p95 | 105 ms | 77 ms | 78 ms |
| p99 | 133 ms | 124 ms | 114 ms |
| max | 202 ms | 178 ms | 221 ms |

| 차수 | 조건 |
| --- | --- |
| Run 1 | 캐시 제거 / SQL 콘솔 로깅 ON / `@Transactional` 없음 |
| Run 2 | + `QueryHomeTodayService.query()` 에 `@Transactional` / SQL 로깅 OFF |
| Run 3 | + 홈 조회 readOnly 트랜잭션 리팩터링 / SQL 로깅 OFF |

**해석**

- **Run 1 → Run 2 (p95 105 → 77, -27%)** — `@Transactional` 통합과 SQL 로깅
  OFF가 *동시에* 바뀌었다. 둘의 기여도는 이 측정만으로 분리 불가. 다만 운영
  프로파일(`application-prod.yml`)은 원래 `show-sql`/`format_sql`을 끄므로,
  **105ms 중 일부는 로컬 설정 아티팩트**였다. 운영 기준 실제 베이스라인은
  처음부터 77ms에 가까웠다.
- **Run 2 → Run 3 (p95 77 → 78)** — readOnly 리팩터링은 **측정 노이즈 안에서
  변화 없음**. 효과 0. (자세한 내용은 [적용한 변경 3](#3-홈-조회-readonly-트랜잭션-리팩터링--측정-효과-없음) 참고.)

> 참고 — 1유저 순차 베이스라인(`HomeTodayBaselineSimulation`, 로깅 ON):
> mean 97ms, p95 155ms. 1.6 rps에 불과해 목표 부하·동시성을 검증하지 못한다.
> 부하가 데워진 10 TPS 측정이 오히려 더 빠른 것은 JIT·커넥션풀 워밍과
> 큰 표본 때문이며, 부하가 병목이 아니라는 방증이다.

---

## 프로파일링 — 56ms는 어디로 가는가

Hibernate `generate_statistics`로 요청 1회의 `Session Metrics`를 캡처했다.

**요청당 DB 쿼리 = 8개, N+1 없음**

| # | 위치 | 쿼리 |
| --- | --- | --- |
| 1 | JWT 필터 (`TokenService.authenticate`) | User 조회 (PK) |
| 2 | `QueryTodayPlanningOverviewService` | DailyPlan + tasks + topPickDetail (fetch join) |
| 3 | 〃 | Timetable + slots (fetch join) |
| 4 | `findNextForHome` | ExamSchedule |
| 5 | `findForHome` | FutureVision |
| 6 | `queryMine` | NotificationPreference |
| 7 | `queryFriendAccountabilityStatus` | AccountabilityRelation |
| 8 | `showFocusTimingCard` | ActivationFunnel |

**시간 분해 (warm 요청 기준)**

| 구간 | 시간 |
| --- | ---: |
| 서비스 — 7개 쿼리 실행 | 26.0 ms |
| 서비스 — statement prepare ×7 | 4.1 ms |
| 서비스 — flush / dirty-check | 8.5 ms |
| JWT 필터 — User 조회 | 4.1 ms |
| **DB/Hibernate 합계** | **~43 ms** |
| 나머지 (보안필터·JWT·직렬화·MVC·VT) | ~13 ms |

**발견**

1. **N+1 없음.** DailyPlan은 `left join tbl_task ... left join tbl_top_pick_detail`
   한 방, Timetable은 `left join tbl_slot` 한 방. 응답 매핑은 fetch join된
   연관만 접근한다.
2. **`@Transactional` 통합 검증됨.** 요청당 `Session Metrics` 블록이 정확히 2개
   (JWT 필터용 1 + 서비스용 1). 서비스의 7개 조회가 `1 JDBC connection`에서
   처리된다. 변경 전이라면 하위 서비스·repo 호출마다 세션이 쪼개져 ~7개였을 것.
3. **인덱스 전부 정상.** DailyPlan(user_id+plan_date 유니크), Timetable(daily_plan_id
   유니크), ExamSchedule(@Index), FutureVision·NotificationPreference·ActivationFunnel
   (user_id 유니크), AccountabilityRelation(양 컬럼 유니크). 슬롯/태스크 FK는
   MySQL이 자동 인덱싱. 추가할 인덱스 없음.
4. **DB가 요청의 ~77%.** 비용의 본질은 8개 BC의 8개 테이블에 대한 **8회 순차
   DB 왕복**이다. BC 격리상 JOIN으로 합칠 수 없다 — 집계형 엔드포인트의
   구조적 바닥값이다.

---

## 적용한 변경

### 1. Redis 캐시 제거

홈 조회 3종 `@Cacheable`, 변경 서비스 6종 `@CacheEvict`/`@CachePut`,
`HomeCacheKey`/`HomeCacheNames`, `RedisCacheConfig`의 홈 캐시 항목을 모두 제거했다.

근거 — 10 TPS 지속에서 캐시 없이 p95 77ms / 실패 0%. 이 규모에서 DB는 압박받지
않으므로 캐시가 "DB 부하를 흡수"한다는 명분이 성립하지 않는다. 게다가 캐시 키가
per-user라 적중률이 낮고, 캐시는 Redis 의존성·직렬화 공격 표면·evict 누락 리스크·
stale 응답이라는 비용을 동반한다. **이 규모에서 캐시는 비용 대비 이득이 없다.**

### 2. `QueryHomeTodayService.query()` 단일 트랜잭션

`query()`에 `@Transactional`이 없어 하위 서비스 4개와 직접 호출 repo 2개가
각자 트랜잭션/커넥션을 잡았다(요청당 ~7세트). `@Transactional` 하나로 8개 조회를
한 커넥션·한 트랜잭션에 묶었다. 프로파일링의 `Session Metrics` 블록 2개가 이를
확인한다.

### 3. 홈 조회 readOnly 트랜잭션 리팩터링 — 측정 효과 없음

`query()`를 `@Transactional(readOnly = true)`로 전환. find-or-create 쓰기
(DailyPlan·NotificationPreference 자동 생성)는 `REQUIRES_NEW` 트랜잭션으로
분리했다. `TodayDailyPlanProvisioner`는 이미 `REQUIRES_NEW`였고, 알림설정용
`NotificationPreferenceProvisioner`를 같은 패턴으로 신규 추가했다(동시 최초진입
unique 위반 복구 포함 — latent race 수정).

프로파일링은 readOnly 전환으로 flush/dirty-check ~8.5ms가 사라질 것으로 추정했다.
**그러나 실측(Run 2 → Run 3)에서 효과는 0이었다.** 그 8.5ms는
`generate_statistics`를 켠 단일 샘플(n=1)에서 나온 값이고, dirty-check 대상이
작은 엔티티 14개뿐이라 실제 임계경로 비용은 무시할 수준이었다. n=1,385의 Gatling
측정이 신뢰할 수 있는 답이며, 그 답은 "변화 없음"이다.

**그럼에도 이 변경은 유지한다 — 성능이 아니라 정합성 근거로.** 읽기 엔드포인트가
readOnly 트랜잭션을 도는 것이 올바른 의미론이고, 새 provisioner는 기존
`TodayDailyPlanProvisioner` 패턴과 일관되며 알림설정 최초생성의 동시성 race를
함께 고쳤다. 이 항목은 "성능 최적화"가 아니라 "정합성·일관성 개선"으로 분류한다.

---

## 결론

- **캐시는 이 규모에 불필요하다.** 10 TPS 지속에서 캐시 없이 p95 77~78ms,
  실패 0%. 측정이 캐시 제거를 정당화했다.
- **p95 105 → 77ms 개선의 상당 부분은 SQL 콘솔 로깅 제거**다 — 운영에선 원래
  꺼져 있던, 로컬 측정 아티팩트였다. `@Transactional` 통합의 순수 기여도는
  로컬에서 분리 측정하지 않았다(운영처럼 DB가 네트워크로 분리되면 트랜잭션 경계
  감소 효과가 더 크다).
- **readOnly 리팩터링은 지연시간 개선이 없었다.** 프로파일링 단일 샘플에 기반한
  예측이 큰 표본 실측에서 기각됐다. 정합성 개선으로만 유지한다.
- **프로파일링 결과 구조적 병목이 없다** — N+1 없음, 누락 인덱스 없음, 중복 조회
  없음. 8회 순차 DB 왕복은 집계 엔드포인트의 바닥값이며 더 줄일 깔끔한 수단이 없다.
- **목표(Peak 10 TPS) 대비 p95 ~78ms / 실패 0%로 충분하다.** `/home/today`는
  추가 최적화가 필요하지 않다.

가장 큰 교훈: 측정 전 추측("캐시가 필요하다", "쿼리를 더 줄여야 한다",
"readOnly가 8.5ms를 절약한다")은 대부분 실측에서 기각됐다. **추측이 아니라 측정이
판단의 근거여야 한다.**

---

## 측정의 한계

- **로컬 단일 머신.** 앱·MySQL·Redis·Gatling이 한 머신에 있다. 운영의 네트워크
  레이턴시·다중 인스턴스는 반영되지 않는다. 다만 10 TPS는 부하 바운드가 아니라
  결론은 바뀌지 않는다.
- **단일 계정 데이터.** 한 테스트 계정 기준. 쿼리 구조(fetch join, 단건 finder)는
  데이터량과 무관하므로 큰 차이는 없을 것으로 본다.
- **표본/한계점.** 베이스라인 n=33, 피크 n=1,385. 동시성 한계점(30/50/100 TPS에서
  언제 무너지는가)은 측정하지 않았다 — 목표가 10 TPS라 범위 밖.

---

## 변경 파일 인덱스

**신규**
- `src/main/java/com/example/movra/bc/notification/application/service/NotificationPreferenceProvisioner.java`
- `src/gatling/java/HomeTodayPeakLoadSimulation.java`

**수정**
- `src/main/java/com/example/movra/bc/home/today/application/service/QueryHomeTodayService.java` — `@Transactional(readOnly = true)`
- `src/main/java/com/example/movra/bc/notification/application/service/QueryNotificationPreferenceService.java` — readOnly + provisioner
- `src/main/java/com/example/movra/bc/visioning/future_vision/application/service/QueryFutureVisionService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/planning/exam_schedule/application/service/QueryExamScheduleService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/notification/application/service/UpdateNotificationPreferenceService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/visioning/future_vision/application/service/CreateFutureVisionService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/visioning/future_vision/application/service/UpdateWeeklyVisionService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/visioning/future_vision/application/service/UpdateYearlyVisionService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/planning/exam_schedule/application/service/CreateExamScheduleService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/planning/exam_schedule/application/service/UpdateExamScheduleService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/bc/planning/exam_schedule/application/service/DeleteExamScheduleService.java` — 홈 캐시 제거
- `src/main/java/com/example/movra/config/cache/RedisCacheConfig.java` — 홈 캐시 항목 제거

**삭제**
- `src/main/java/com/example/movra/config/cache/HomeCacheNames.java`
- `src/main/java/com/example/movra/config/cache/HomeCacheKey.java`

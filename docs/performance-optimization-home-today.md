# Home 조회 API 성능 최적화

> Cache Validation & Profiling & Transaction Optimization / Redis Removal / Gatling

---

## 배경

`GET /home/today`는 여러 Bounded Context(미래비전·일일계획·시간표·시험일정·알림설정·친구책임·집중타이밍)를 집계하며, 요청 1건당 **8회 이상의 순차 DB 조회**가 발생하는 구조다.

조회 성능 개선을 목적으로 **Redis Cache 3종**(FutureVision, NotificationPreference, ExamSchedule)을 도입해 초기 설계했다.

| 캐시 이름 | 대상 메서드 | 키 | TTL | Evict 경로 수 |
| --- | --- | --- | --- | --- |
| `home:v2:future-vision` | `findForHome` | `userId` | 12h | 3 (`@CacheEvict`) |
| `home:v2:notification-preference` | `queryMine` | `userId` | 12h | 1 (`@CachePut`) |
| `home:v2:next-exam-schedule` | `findNextForHome` | `userId:today` | 6h | 3 (`@CacheEvict`) |

**목표 규모(DAU 150, Peak 10 TPS, 조회 중심)에서 이 캐시가 실제로 필요한지**를 수치로 검증하기 전에 제거 여부를 결정하기는 어렵다. 측정으로 판단하기로 했다.

---

## 접근

### Gatling A/B 시뮬레이션

`HomeTodayPeakLoadSimulation`으로 **캐시 ON/OFF 두 환경을 동일 조건에서 측정**했다.

| 항목 | 값 |
| --- | --- |
| 시뮬레이션 | warmup 2 req/s(10s) → 1→10 req/s 램프(30s) → 10 req/s 지속(120s) |
| 총 요청 수 | 1,385건 |
| 측정 환경 | 로컬 단일 머신 (앱·MySQL·Redis·Gatling 동거), Spring Boot 3.5, Java 21, Virtual Threads on |
| SQL 로깅 | OFF (운영 기준) |

캐시 ON 측정은 `feat/cache-ab-test` 브랜치(캐시 코드 복원)로, 캐시 OFF는 `main` 브랜치로 서버를 각각 실행했다. Redis는 측정 전 FLUSHDB로 초기화.

### Hibernate 세션 프로파일링

`generate_statistics`를 활성화해 요청 1회의 `Session Metrics`를 캡처, 쿼리 수·실행 시간·커넥션 경계를 분해했다.

### Redis MONITOR

캐시 ON 서버에서 `/home/today` 요청 1건당 발생하는 Redis 명령어를 직접 캡처해 실제 캐시 적중 패턴을 확인했다.

---

## 해결

### 1. Redis 캐시 제거 — A/B 측정 근거

A/B 측정 결과:

| 지표 | Run 0-warm (캐시 ON) | Run 2-fresh (캐시 없음, cold) | Run 2 (캐시 없음, warm) |
| --- | ---: | ---: | ---: |
| mean | 55 ms | 53 ms | 56 ms |
| p50 | 53 ms | 40 ms | 54 ms |
| p75 | 57 ms | 46 ms | 60 ms |
| p95 | **74 ms** | **66 ms** | **77 ms** |
| p99 | **105 ms** | **601 ms** | **124 ms** |
| max | 212 ms | 1,615 ms | 178 ms |
| σ | **13 ms** | **106 ms** | — |
| 실패율 | 0% | 0% | 0% |

**캐시 ON이 p95에서 오히려 느리다(74ms vs 66–77ms).** 원인은 테스트 계정의 데이터 구성이다.

Redis MONITOR 캡처:

```
캐시 ON — 요청 1건당
  GET home:v2:next-exam-schedule::{userId}:{today}   → miss (시험일정 미설정)
  GET home:v2:future-vision::{userId}                → miss (미래비전 미설정)
  GET home:v2:notification-preference::{userId}      → hit
캐시 없음 — Redis 명령어 0회
```

| 캐시 항목 | 결과 | 저장 여부 |
| --- | --- | --- |
| `future-vision` | `Optional.empty()` | ❌ `unless="#result==null"` 조건으로 미저장 |
| `next-exam-schedule` | `Optional.empty()` | ❌ `unless="#result==null"` 조건으로 미저장 |
| `notification-preference` | 존재 | ✅ 저장, 이후 요청은 Redis GET hit |

캐시 3종 중 실효 적중은 1개. 나머지 2개는 매 요청마다 Redis GET miss 후 DB 조회(캐시 없음과 동일)이고, Spring AOP `@Cacheable` 프록시 오버헤드 ×3이 추가된다. **AOP 오버헤드가 쿼리 1개 절약보다 크다.**

p99/max/σ의 캐시 ON 우위(p99 105ms vs 124ms, σ 13ms vs 106ms)는 JIT 콜드스타트 스파이크 흡수 효과다. 워밍된 서버 기준으로 차이는 19ms로 좁혀진다.

**이 규모(DAU 150, 10 TPS)에서 DB는 전혀 압박받지 않는다. 캐시가 "DB 부하를 흡수"한다는 명분이 성립하지 않는다.** 캐시가 실질 이득을 내는 임계는 수천 TPS 이상이다.

제거로 절감된 코드·리스크:

| 축 | 절감량 |
| --- | --- |
| Redis 명령어 | 2,400회/일, 30 ops/s(피크) → 0 |
| 삭제된 코드 | **111줄, 13개 파일** |
| 제거된 어노테이션 | **10개** (`@Cacheable` ×3, `@CacheEvict` ×6, `@CachePut` ×1) |
| 제거된 Evict 경로 | **6개 서비스** (누락 시 stale 응답 유발) |
| 제거된 장애 모드 | Redis 장애 영향, stale 응답, Jackson 직렬화 취약점 경로 |

### 2. `QueryHomeTodayService.query()` 단일 트랜잭션

`query()`에 `@Transactional`이 없어 하위 서비스 4개와 직접 호출 repo 2개가 각자 트랜잭션/커넥션을 잡았다(요청당 ~7세트). `@Transactional` 하나로 8개 조회를 **한 커넥션·한 트랜잭션에 묶었다.** 프로파일링의 `Session Metrics` 블록이 정확히 2개(JWT 필터용 1 + 서비스용 1)인 것이 이를 확인한다.

### 3. readOnly 리팩터링 + Provisioner 분리 (정합성 개선)

`query()`를 `@Transactional(readOnly = true)`로 전환. find-or-create 쓰기(DailyPlan·NotificationPreference 자동 생성)는 `REQUIRES_NEW` 트랜잭션으로 분리했다. `NotificationPreferenceProvisioner`를 신규 추가해 `TodayDailyPlanProvisioner`와 동일한 패턴(동시 최초진입 unique 위반 복구 포함)을 맞췄다.

프로파일링은 flush/dirty-check ~8.5ms 절약을 예측했으나, **실측(Run 2 → Run 3)에서 p95 변화는 77 → 78ms — 측정 노이즈 안에서 효과 없음.** 8.5ms는 n=1 단일 샘플의 아티팩트였고, dirty-check 대상 엔티티 14개의 실제 비용은 무시할 수준이다. 이 변경은 **성능이 아니라 readOnly 의미론 일관성과 동시성 race 수정 근거로 유지한다.**

---

## 결과

### 성능 수치

| 지표 | Run 1 (초기) | Run 2 (최적화 후) |
| --- | ---: | ---: |
| mean | 77 ms | 56 ms |
| p50 | 75 ms | 54 ms |
| p75 | 85 ms | 60 ms |
| p95 | **105 ms** | **77 ms (−27%)** |
| p99 | 133 ms | 124 ms |
| max | 202 ms | 178 ms |
| 실패율 | 0% | 0% |

> Run 1 조건: 캐시 제거 직후, SQL 콘솔 로깅 ON. p95 105ms 중 일부는 로컬 설정 아티팩트(운영 `application-prod.yml`은 `show-sql`/`format_sql` 비활성). `@Transactional` 통합과 로깅 OFF의 기여도는 이 측정만으로 분리 불가.

**목표(Peak 10 TPS) 대비 p95 66–77ms / 실패율 0% — 추가 최적화 불필요.**

### 프로파일링 요약 — 요청당 DB 쿼리 8개, N+1 없음

| # | 위치 | 쿼리 |
| --- | --- | --- |
| 1 | JWT 필터 | User 조회 (PK) |
| 2 | `QueryTodayPlanningOverviewService` | DailyPlan + tasks + topPickDetail (fetch join) |
| 3 | 〃 | Timetable + slots (fetch join) |
| 4 | `findNextForHome` | ExamSchedule |
| 5 | `findForHome` | FutureVision |
| 6 | `queryMine` | NotificationPreference |
| 7 | `queryFriendAccountabilityStatus` | AccountabilityRelation |
| 8 | `showFocusTimingCard` | ActivationFunnel |

DB/Hibernate 합계 ~43ms, 나머지(필터·JWT·직렬화·MVC) ~13ms. **8회 순차 DB 왕복은 집계 엔드포인트의 구조적 바닥값이다** — BC 격리상 JOIN으로 합칠 수 없다. 추가할 인덱스 없음.

### 교훈

측정 전 추측("캐시가 빠르게 만든다", "캐시가 필요하다", "readOnly가 8.5ms를 절약한다")은 모두 실측에서 기각됐다. **캐시는 빠르게 만드는 도구가 아니라 DB 부하를 흡수하는 도구**다. DB가 압박받지 않는 규모에서 캐시는 복잡성 비용만 남긴다. 추측이 아니라 측정이 판단의 근거여야 한다.

---

## 변경 파일 인덱스

**신규**
- `bc/notification/application/service/NotificationPreferenceProvisioner.java`
- `src/gatling/java/HomeTodayPeakLoadSimulation.java`

**수정**
- `bc/home/today/application/service/QueryHomeTodayService.java` — `@Transactional(readOnly = true)`
- `bc/notification/application/service/QueryNotificationPreferenceService.java` — readOnly + provisioner
- `bc/visioning/future_vision/application/service/QueryFutureVisionService.java` — 홈 캐시 제거
- `bc/planning/exam_schedule/application/service/QueryExamScheduleService.java` — 홈 캐시 제거
- `bc/notification/application/service/UpdateNotificationPreferenceService.java` — 홈 캐시 제거
- `bc/visioning/future_vision/application/service/CreateFutureVisionService.java` — 홈 캐시 제거
- `bc/visioning/future_vision/application/service/UpdateWeeklyVisionService.java` — 홈 캐시 제거
- `bc/visioning/future_vision/application/service/UpdateYearlyVisionService.java` — 홈 캐시 제거
- `bc/planning/exam_schedule/application/service/CreateExamScheduleService.java` — 홈 캐시 제거
- `bc/planning/exam_schedule/application/service/UpdateExamScheduleService.java` — 홈 캐시 제거
- `bc/planning/exam_schedule/application/service/DeleteExamScheduleService.java` — 홈 캐시 제거
- `config/cache/RedisCacheConfig.java` — 홈 캐시 항목 제거

**삭제**
- `config/cache/HomeCacheNames.java`
- `config/cache/HomeCacheKey.java`

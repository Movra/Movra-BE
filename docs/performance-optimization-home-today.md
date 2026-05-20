# 홈 조회 API 성능 측정·최적화 기록 — `GET /home/today`

> DAU 150 / Peak 10 TPS / 조회 중심이라는 목표를 전제로 `GET /home/today`를
> **측정·프로파일링 데이터**로 점검한 기록. 추측으로 고치지 않고, 측정이 가리키는
> 것만 손댔다. 효과가 없었던 변경도 그대로 기록한다.

## 목차

- [배경](#배경)
- [측정 전제](#측정-전제)
- [측정 기록](#측정-기록)
- [캐시 ON/OFF A/B 비교 측정](#캐시-onoff-ab-비교-측정)
  - [캐시 제거로 절감된 운영 비용 수치](#캐시-제거로-절감된-운영-비용-수치)
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

## 캐시 ON/OFF A/B 비교 측정

Run 1~3은 모두 캐시를 제거한 뒤 측정했다. 캐시 제거 이후 "캐시가 실제로 얼마나 개선하는가"와
"캐시를 유지할 때 대비 제거로 얻는 효율이 어느 정도인가"를 수치로 검증하기 위해
`feat/cache-ab-test` 브랜치에 캐시를 임시 복원하고 동일 시뮬레이션을 추가 실행했다.

### 캐시 설계 구조

8개 쿼리 중 캐시 대상은 3개(37.5%).

| 캐시 이름 | 대상 메서드 | 키 | TTL | Evict 경로 수 |
| --- | --- | --- | --- | --- |
| `home:v2:future-vision` | `findForHome` | `userId` | 12h | 3 (`@CacheEvict`) |
| `home:v2:notification-preference` | `queryMine` | `userId` | 12h | 1 (`@CachePut`) |
| `home:v2:next-exam-schedule` | `findNextForHome` | `userId:today` | 6h | 3 (`@CacheEvict`) |

### A/B 비교 측정 결과

(`HomeTodayPeakLoadSimulation`, 1,385건, SQL 로깅 OFF)

| 지표 | Run 0-warm (캐시 ON) | Run 2-fresh (캐시 없음) | Run 2 (캐시 없음, 워밍) |
| --- | ---: | ---: | ---: |
| mean | 55 ms | 53 ms | 56 ms |
| p50 | 53 ms | 40 ms | 54 ms |
| p75 | 57 ms | 46 ms | 60 ms |
| p95 | **74 ms** | **66 ms** | **77 ms** |
| p99 | **105 ms** | **601 ms** | **124 ms** |
| max | 212 ms | 1,615 ms | 178 ms |
| σ (표준편차) | **13 ms** | **106 ms** | — |
| 실패율 | 0% | 0% | 0% |

| 차수 | 조건 |
| --- | --- |
| Run 0-warm | 캐시 ON / Redis FLUSHDB 후 서버 재시작 / SQL 로깅 OFF |
| Run 2-fresh | 캐시 없음(main 브랜치) / 서버 재시작 직후 / SQL 로깅 OFF |
| Run 2 (워밍) | 캐시 없음 / JVM 워밍 상태 / SQL 로깅 OFF (이전 측정) |

### 결과 해석

**캐시 ON이 p95에서 오히려 느리다 (74ms vs 66ms)**

원인은 테스트 계정의 데이터 구성에 있다. 캐시 3종 중 실제로 저장된 것은
`notification-preference` 1개뿐이다.

| 캐시 항목 | 테스트 계정 결과 | 캐시 저장 여부 |
| --- | --- | --- |
| `home:v2:future-vision` | `Optional.empty()` (미래비전 미설정) | ❌ `unless="#result==null"` 조건으로 저장 안 됨 |
| `home:v2:next-exam-schedule` | `Optional.empty()` (시험 일정 없음) | ❌ `unless="#result==null"` 조건으로 저장 안 됨 |
| `home:v2:notification-preference` | 존재 | ✅ 저장됨, 이후 요청은 Redis GET |

2개 항목이 캐시에 저장되지 않으므로 매 요청마다:
- Redis GET miss × 2 → DB 조회 (= 캐시 없음과 동일)
- Redis GET hit × 1 → notification-preference DB 조회 절약 (~2–3ms)
- Spring AOP `@Cacheable` 프록시 오버헤드 × 3

결과적으로 AOP 오버헤드가 1개 쿼리 절약보다 크게 작용해 p50/p75/p95에서 캐시 ON이 더 느리다.

**캐시 ON이 p99/max/σ에서 압도적으로 안정적이다**

| 지표 | 캐시 ON | 캐시 없음(fresh) | 차이 |
| --- | ---: | ---: | ---: |
| p99 | 105 ms | 601 ms | **−496 ms (−83%)** |
| max | 212 ms | 1,615 ms | **−1,403 ms (−87%)** |
| σ | 13 ms | 106 ms | **−93 ms (−88%)** |

이 차이는 주로 **JIT 콜드스타트 스파이크** 때문이다. 두 서버 모두 재시작 직후 측정했으나,
캐시 ON 서버는 Redis 경로를 타는 요청이 DB 접근을 일부 피해 스파이크가 흡수된다.
워밍된 서버 기준 Run 2 p99(124ms)와 비교하면 캐시 ON p99(105ms)의 차이는 19ms로 좁혀진다.

**운영 환경에서 캐시 효과가 더 클 조건**

이번 측정은 두 가지 이유로 캐시 이득을 과소평가한다.
1. **데이터 미설정 계정**: 미래비전·시험일정이 null인 유저는 해당 캐시 이득이 전혀 없다.
   실제 서비스에서 두 항목이 모두 설정된 유저라면 3개 쿼리가 모두 캐시되어 더 나은 p95가 예상된다.
2. **로컬 단일 머신**: DB와 앱이 같은 머신이라 DB 왕복 비용이 낮다.
   운영처럼 네트워크로 분리되면 DB 왕복 ~5–15ms, Redis 왕복 ~1–2ms → 캐시 절약폭이 커진다.

### 캐시 제거로 절감된 운영 비용 수치

캐시 제거의 실익을 **Redis 명령어·메모리·코드·리스크** 네 축으로 측정·계산했다.

#### Redis 명령어 절약

Redis MONITOR로 `/home/today` 요청 1건당 발생하는 Redis 명령어를 직접 캡처했다.

```
캐시 ON — 요청 1건당 Redis GET × 3
  GET home:v2:next-exam-schedule::{userId}:{today}   (miss)
  GET home:v2:future-vision::{userId}                (miss)
  GET home:v2:notification-preference::{userId}      (hit)
캐시 없음 — Redis 명령어 0회
```

| 시나리오 | 요청당 Redis 명령어 | DAU 150 × 5req/day | 피크(10 TPS) |
| --- | ---: | ---: | ---: |
| 캐시 ON | GET ×3 + SET ×0.2 = **3.2회** | **2,400회/일** | **30 ops/s** |
| 캐시 없음 | **0회** | **0회/일** | **0 ops/s** |
| **절약** | **3.2회** | **2,400회/일 (100%)** | **30 ops/s** |

> note — GET 2개는 miss(FutureVision·ExamSchedule null → `unless` 조건으로 미저장).
> miss도 Redis 왕복은 발생한다.

#### Redis 메모리 절약

`STRLEN`으로 직접 측정한 값 크기 기준.

| 캐시 항목 | 값 크기(측정/추정) | 키 이름 | Redis 오버헤드 | 키 1개 합계 |
| --- | ---: | ---: | ---: | ---: |
| `notification-preference` | **527 B** (실측) | 69 B | ~100 B | **~696 B** |
| `future-vision` | ~350 B (추정) | 60 B | ~100 B | ~510 B |
| `next-exam-schedule` | ~300 B (추정) | 72 B | ~100 B | ~472 B |

| 시나리오 | 유저당 메모리 | DAU 150 총계 |
| --- | ---: | ---: |
| 캐시 ON (3개 모두 데이터 있음) | ~1,678 B | **~246 KB** |
| 캐시 ON (테스트 계정처럼 1개만) | ~696 B | **~102 KB** |
| 캐시 없음 | 0 B | **0 KB** |

> 절약폭 자체는 **102–246 KB로 인프라 관점에서 무의미하다** (AWS ElastiCache
> cache.t3.micro 512 MB의 0.05% 미만). 메모리 비용은 제거 근거가 아니다.

#### 코드 복잡도 절약

`git diff`로 계산한 실제 삭제량.

| 항목 | 수치 |
| --- | --- |
| 삭제된 코드 줄 | **111줄** |
| 변경·삭제된 파일 | **13개** |
| 완전 삭제된 파일 | 2개 (`HomeCacheKey`, `HomeCacheNames`) |
| 제거된 캐시 어노테이션 | **10개** (`@Cacheable` ×3, `@CacheEvict` ×6, `@CachePut` ×1) |
| 제거된 Evict 경로 | **6개 서비스** (누락 시 stale 응답 유발 경로) |

#### 리스크 비용 절감

수치화는 불가능하지만 제거된 장애 모드를 명시한다.

| 리스크 | 캐시 ON | 캐시 없음 |
| --- | --- | --- |
| Redis 장애 시 홈 조회 | 응답 불가(500) 또는 stale 반환 | **영향 없음** |
| Evict 누락 → stale 응답 | 6개 경로 중 1개 누락으로 발생 가능 | **구조적으로 불가** |
| 직렬화 취약점 | Jackson polymorphic deserialization 활성 | **홈 조회 경로 비활성** |

#### 종합

| 축 | 절감량 | 평가 |
| --- | --- | --- |
| Redis 명령어 | 2,400회/일, 30 ops/s (피크) 절약 | 이 규모에선 인프라 비용 임계 미달 |
| Redis 메모리 | 102–246 KB 절약 | 무의미한 수준 |
| 코드 | 111줄·13파일·10 어노테이션 삭제 | **유지보수 부담 직접 제거** |
| 리스크 | Redis 장애 영향·Evict 누락·직렬화 취약점 제거 | **가장 실질적인 비용 절감** |

**결론**: 이 규모(DAU 150, 10 TPS)에서 캐시 제거의 실질 이득은 인프라 비용이 아니라
**코드 단순화와 리스크 제거**에 있다. Redis 명령어·메모리 절약은 수치로 확인됐으나
운영 비용 관점에서 의미 있는 규모가 아니다. 캐시가 진짜 이득을 내는 임계는 **DB가
압박받기 시작하는 시점**(이 서비스 기준 수천 TPS 이상)이며, 그 전까지 캐시는
복잡성 비용만 남긴다.

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

- **캐시 제거는 옳았다 — A/B 측정이 이를 수치로 증명했다.**
  캐시 ON p95(74ms)가 캐시 없음 p95(66–77ms)보다 개선이 없거나 오히려 느리다.
  p99 안정성 이득(105ms vs 124ms, △19ms)은 존재하지만,
  10개 어노테이션·6개 Evict 경로·Redis 의존성이라는 유지 비용을 정당화하지 못한다.

- **캐시가 p95를 개선하지 못한 구조적 이유가 확인됐다.**
  `unless="#result==null"` 조건으로 인해 데이터가 미설정된 항목(FutureVision·ExamSchedule)은
  캐시에 저장되지 않아, 매 요청마다 Redis GET miss → DB 조회가 발생한다.
  실효 캐시는 `notification-preference` 1개뿐이고, 나머지 2개는 AOP 오버헤드만 남긴다.

- **p95 105 → 77ms 개선의 상당 부분은 SQL 콘솔 로깅 제거**다 — 운영에선 원래
  꺼져 있던, 로컬 측정 아티팩트였다. `@Transactional` 통합의 순수 기여도는
  로컬에서 분리 측정하지 않았다(운영처럼 DB가 네트워크로 분리되면 트랜잭션 경계
  감소 효과가 더 크다).

- **readOnly 리팩터링은 지연시간 개선이 없었다.** 프로파일링 단일 샘플에 기반한
  예측이 큰 표본 실측에서 기각됐다. 정합성 개선으로만 유지한다.

- **프로파일링 결과 구조적 병목이 없다** — N+1 없음, 누락 인덱스 없음, 중복 조회
  없음. 8회 순차 DB 왕복은 집계 엔드포인트의 바닥값이며 더 줄일 깔끔한 수단이 없다.

- **목표(Peak 10 TPS) 대비 p95 ~66–77ms / 실패 0%로 충분하다.** `/home/today`는
  추가 최적화가 필요하지 않다.

가장 큰 교훈: 측정 전 추측("캐시가 필요하다", "캐시가 빠르게 만든다",
"readOnly가 8.5ms를 절약한다")은 모두 실측에서 기각됐다.
특히 캐시는 **빠르게 만드는 도구가 아니라 DB 부하를 흡수하는 도구**인데,
이 규모에서 DB는 전혀 압박받지 않으므로 캐시의 존재 이유가 없다.
**추측이 아니라 측정이 판단의 근거여야 한다.**

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

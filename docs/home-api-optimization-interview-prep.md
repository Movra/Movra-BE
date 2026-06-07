# Home 조회 API 최적화 — 면접 대비 심층 분석

> `docs/performance-optimization-home-today.md`에 기록된 최적화 작업을 면접에서
> 정확하게 설명할 수 있도록 각 결정의 **왜(Why)·어떻게(How)·무엇(What)**을 분해한 문서.

---

## 목차

1. [A/B 테스트로 캐시 효과를 검증한 방식](#1-ab-테스트로-캐시-효과를-검증한-방식)
2. [캐시가 오히려 느렸던 이유 — `unless="#result==null"` 함정](#2-캐시가-오히려-느렸던-이유--unlessresultnull-함정)
3. [단일 `@Transactional`이 성능에 미친 영향](#3-단일-transactional이-성능에-미친-영향)
4. [Hibernate Session Metrics 프로파일링](#4-hibernate-session-metrics-프로파일링)
5. [Redis MONITOR로 캐시 흐름 추적](#5-redis-monitor로-캐시-흐름-추적)
6. [`readOnly` 트랜잭션 + REQUIRES_NEW Provisioner 패턴](#6-readonly-트랜잭션--requires_new-provisioner-패턴)
7. [캐시는 "빠르게 만드는 도구"가 아니다 — 핵심 교훈](#7-캐시는-빠르게-만드는-도구가-아니다--핵심-교훈)
8. [수치 총정리 — 면접 암기용](#8-수치-총정리--면접-암기용)

---

## 1. A/B 테스트로 캐시 효과를 검증한 방식

### 왜 A/B 테스트가 필요했나

캐시를 제거한 이후 "캐시가 없어도 충분히 빠르다"는 것만 측정했고, **"캐시가 있을 때와 비교해서 얼마나 다른가"는 수치가 없었다.** 캐시 제거 결정에 직접적인 비교 근거가 없다는 문제가 있었다.

단순히 "10 TPS에서 p95 77ms니까 캐시 없어도 된다"는 주장은 추론이다. 측정이 필요했다.

### 어떻게 A/B 테스트를 구성했나

```
캐시 ON  → feat/cache-ab-test 브랜치 (캐시 코드 복원) → 서버 재시작
캐시 OFF → main 브랜치 (캐시 제거 상태)              → 서버 재시작

공통 조건:
  - 동일 시뮬레이션: HomeTodayPeakLoadSimulation
  - 동일 부하: warmup 2 req/s(10s) → 1→10 req/s 램프(30s) → 10 req/s 지속(120s)
  - 동일 계정, 동일 데이터
  - SQL 로깅 OFF (운영 기준)
  - Redis FLUSHDB 후 시작 (캐시 ON 측정 전 초기화)
```

**"동일 조건"이 핵심이다.** 부하 패턴·데이터·로깅 설정이 달라지면 캐시 효과가 아닌 다른 변수의 효과를 측정하게 된다.

### 왜 p95를 기준 지표로 삼았나

- **mean(평균)**: JIT 콜드스타트 스파이크처럼 극단값에 덜 민감해 실제 사용자 경험을 과소평가한다.
- **p99**: 드문 이상값(GC, 커넥션 경쟁)에 지나치게 민감하다.
- **p95**: 요청 100개 중 95번째로 느린 응답 — 일상 트래픽에서 실제 사용자가 경험하는 "느린 편"의 대표값이다. 실서비스 SLA 기준으로도 가장 많이 쓰인다.

### 결과가 의미하는 것

| 지표 | 캐시 ON | 캐시 OFF (warm) |
| --- | ---: | ---: |
| p95 | 74 ms | 77 ms |
| p99 | 105 ms | 124 ms |

p95는 캐시 ON이 74ms, 캐시 OFF가 77ms — **캐시가 3ms 빠르지만 통계적으로 의미 없는 수준이다.** 반대로 캐시 없음 cold 상태(서버 재시작 직후)에서는 p99 601ms로 크게 튀는데, 이는 JIT 워밍 전 스파이크가 캐시 ON에서는 Redis 경로로 일부 흡수되는 효과다. 워밍 후에는 19ms 차이로 좁혀진다.

---

## 2. 캐시가 오히려 느렸던 이유 — `unless="#result==null"` 함정

### 코드 수준에서 무슨 일이 일어났나

캐시 ON 상태에서 `@Cacheable` 어노테이션에 `unless="#result==null"` 조건이 달려 있었다.

```java
// QueryFutureVisionService
@Cacheable(
    cacheNames = HomeCacheNames.FUTURE_VISION,
    key = "@homeCacheKey.currentUserId()",
    unless = "#result == null"  // ← 이 조건
)
public Optional<FutureVisionResponse> findForHome() { ... }
```

`unless="#result==null"`의 의미: **결과가 null이면 Redis에 저장하지 않는다.**

테스트 계정은 미래비전과 시험일정을 등록하지 않은 상태였다. 메서드가 `Optional.empty()`를 반환하면 이 조건에 걸려 Redis에 저장되지 않는다.

### 실제 요청 흐름

```
매 요청마다 (캐시 ON):

1. Spring AOP → Redis GET future-vision key       → MISS (empty Optional은 저장 안 됨)
2. DB 쿼리 실행 → Optional.empty() 반환
3. unless 조건 → Redis SET 하지 않음

4. Spring AOP → Redis GET exam-schedule key        → MISS (동일 이유)
5. DB 쿼리 실행 → Optional.empty() 반환
6. unless 조건 → Redis SET 하지 않음

7. Spring AOP → Redis GET notification-preference  → HIT (데이터 있음)
   DB 쿼리 생략, Redis 값 반환

---

매 요청마다 (캐시 OFF):

1. DB 쿼리 실행 → Optional.empty() 반환
2. DB 쿼리 실행 → Optional.empty() 반환
3. DB 쿼리 실행 → NotificationPreference 반환
```

### 왜 캐시 ON이 더 느린가

| 항목 | 캐시 ON | 캐시 OFF |
| --- | --- | --- |
| DB 쿼리 수 | 2회 (miss → DB fallback) | 3회 |
| Redis 왕복 | 3회 (GET ×3) | 0회 |
| AOP 프록시 오버헤드 | 3회 | 0회 |

캐시가 절약하는 것: DB 쿼리 1회 (notification-preference)
캐시가 추가하는 것: Redis 왕복 3회 + AOP 오버헤드 3회

**로컬 환경에서 Redis와 DB가 같은 머신에 있을 때 Redis 왕복과 DB 쿼리의 비용 차이가 작다.** DB 쿼리 1회가 ~3–4ms, Redis GET이 ~0.1ms라고 해도, AOP 프록시가 메서드당 인터셉터 체인을 실행하는 오버헤드가 누적되면 오히려 느려질 수 있다.

### 면접에서 이 구조를 어떻게 설명하나

> "캐시가 항상 빠른 것은 아닙니다. `unless="#result==null"` 조건으로 설계된 캐시는 null 결과를 저장하지 않습니다. 해당 데이터가 없는 사용자는 매 요청마다 Redis GET miss → DB 조회가 발생하므로, Redis 왕복 비용과 AOP 오버헤드만 추가됩니다. 실제로 측정해보니 이 케이스에서 캐시 ON(p95 74ms)이 캐시 OFF(p95 66ms)보다 오히려 느렸습니다."

---

## 3. 단일 `@Transactional`이 성능에 미친 영향

### Spring 트랜잭션 전파의 기본 동작

Spring `@Transactional`의 기본 전파 속성은 `REQUIRED`다. 이는 "이미 트랜잭션이 있으면 합류, 없으면 새로 시작"이다.

`QueryHomeTodayService.query()`에 `@Transactional`이 없으면 어떻게 되나:

```
query() 호출 (트랜잭션 없음)
  ├── queryTodayPlanningOverview() → @Transactional(REQUIRED) → 새 트랜잭션 A 시작 → JDBC Connection #1
  │     └── 트랜잭션 A 종료, Connection #1 반납
  ├── findNextForHome() → @Transactional(REQUIRED) → 새 트랜잭션 B 시작 → JDBC Connection #2
  │     └── 트랜잭션 B 종료, Connection #2 반납
  ├── findForHome() → @Transactional(REQUIRED) → 새 트랜잭션 C → JDBC Connection #3
  ...
```

즉, 조회 1회에 **7개 이상의 JDBC Connection 획득·반납 사이클**이 발생한다. 커넥션 풀에서 Connection을 가져오고 돌려주는 비용이 반복된다.

### `@Transactional`을 추가하면

```
query() 호출 → @Transactional(REQUIRED) → 새 트랜잭션 시작 → JDBC Connection #1

  ├── queryTodayPlanningOverview() → @Transactional(REQUIRED) → 상위 트랜잭션 합류 (Connection #1 재사용)
  ├── findNextForHome() → @Transactional(REQUIRED) → 상위 트랜잭션 합류 (Connection #1 재사용)
  ├── findForHome() → @Transactional(REQUIRED) → 상위 트랜잭션 합류 (Connection #1 재사용)
  ...

query() 종료 → 트랜잭션 커밋/롤백 → Connection #1 반납
```

**8개 조회 → 1개 JDBC Connection, 1개 트랜잭션.** Connection 획득·반납이 1번으로 줄어든다.

### Hibernate Session Metrics로 확인

`generate_statistics=true`를 켜면 로그에 `Session Metrics` 블록이 찍힌다.

- `@Transactional` 추가 전: 요청 1회에 `Session Metrics` 블록이 6–7개 (서비스마다 새 Session)
- `@Transactional` 추가 후: 블록이 정확히 **2개** (JWT 필터용 Session 1개 + 서비스 전체 Session 1개)

이 숫자가 "8개 조회가 1개 Connection에서 처리됐다"는 직접적인 증거다.

### 왜 성능이 좋아지나

커넥션 풀(HikariCP)에서 Connection을 가져오는 것은 단순 객체 참조가 아니다. 소켓이 이미 열려 있어도 커넥션 유효성 체크, 풀 락 획득, 트랜잭션 시작 등의 오버헤드가 있다. 이것이 7번 → 1번으로 줄어든다. 운영 환경처럼 DB가 네트워크로 분리되어 있으면 이 효과가 더 커진다.

---

## 4. Hibernate Session Metrics 프로파일링

### 어떻게 활성화했나

```yaml
# application.yml (로컬/테스트 환경)
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
```

```java
// 로그 레벨 설정 (logback 또는 application.yml)
logging:
  level:
    org.hibernate.stat: DEBUG
```

### 무엇을 보여주나

요청 1회에 대한 Hibernate Session 통계:

```
Session Metrics {
  14 nanoseconds spent acquiring 1 JDBC connections;
  0 nanoseconds spent releasing 0 JDBC connections;
  1,234,567 nanoseconds spent preparing 7 JDBC statements;
  8,765,432 nanoseconds spent executing 7 JDBC statements;
  0 nanoseconds spent executing 0 JDBC batches;
  0 nanoseconds spent performing 0 L2C puts;
  0 nanoseconds spent performing 0 L2C hits;
  0 nanoseconds spent performing 0 L2C misses;
  8,500,000 nanoseconds spent executing 1 flushes (dirty checking 14 entities);
}
```

이 출력으로 확인한 것:
- `1 JDBC connections` → `@Transactional` 통합이 실제로 동작함
- `7 JDBC statements` → 8개 조회 중 서비스 레이어 7개 (JWT 필터는 별도 Session)
- `14 entities dirty checking` → readOnly 전환 전 flush 비용의 규모
- `0 L2C` → 2차 캐시(Hibernate Second-Level Cache) 미사용

### 시간 분해 결과

| 구간 | 측정값 |
| ---: | ---: |
| 서비스 — 7개 쿼리 실행 | 26.0 ms |
| 서비스 — statement prepare ×7 | 4.1 ms |
| 서비스 — flush/dirty-check (14 entities) | 8.5 ms |
| JWT 필터 — User 조회 | 4.1 ms |
| **DB/Hibernate 합계** | **~43 ms** |
| 나머지 (보안필터·직렬화·MVC) | ~13 ms |
| **총합** | **~56 ms** |

이 분석으로 "8.5ms flush 비용을 `readOnly`로 제거할 수 있다"는 가설을 세웠다. 그러나 실측(Gatling n=1,385)에서 효과가 없었다 → **n=1 단일 샘플은 노이즈가 많아 신뢰할 수 없다.** 큰 표본의 Gatling 측정이 더 신뢰할 수 있는 답이다.

---

## 5. Redis MONITOR로 캐시 흐름 추적

### MONITOR가 무엇인가

Redis MONITOR는 Redis 서버가 받는 **모든 명령을 실시간으로 출력**하는 디버깅 도구다.

```
redis-cli MONITOR
```

서버가 처리하는 GET, SET, DEL 등 모든 명령이 타임스탬프와 함께 찍힌다. 이것으로 "요청 1회에 Redis 명령이 몇 번 발생하는가"를 직접 눈으로 확인할 수 있다.

### 캡처 결과

`/home/today` 요청 1회에 대한 MONITOR 출력 (캐시 ON):

```
1716194400.123 [0 127.0.0.1:54321] "GET" "home:v2:next-exam-schedule::userId:2026-05-20"
1716194400.134 [0 127.0.0.1:54321] "GET" "home:v2:future-vision::userId"
1716194400.145 [0 127.0.0.1:54321] "GET" "home:v2:notification-preference::userId"
```

3번의 GET, SET 없음 → 2개는 miss, 1개는 hit이지만 SET이 없으므로 새로 저장되지 않음.

### STRLEN으로 메모리 측정

Redis에 `MEMORY USAGE` 명령이 없는 환경(구버전 Redis)에서는 `STRLEN`으로 값 크기를 근사한다.

```
redis-cli STRLEN "home:v2:notification-preference::userId"
→ 527
```

527바이트(JSON 직렬화된 값) + 키 이름 길이 + Redis 내부 오버헤드(~100B) ≈ **696B/유저**.

### 왜 MONITOR를 썼나 — 면접 설명

> "코드를 보면 `@Cacheable`이 적용되어 있으니 캐시가 동작하는 것 같지만, 실제로 Redis에서 어떤 명령이 몇 번 발생하는지는 코드만 봐서는 알 수 없습니다. MONITOR로 직접 추적해서 '요청당 GET 3회, SET 0회'임을 확인했습니다. 이 수치가 `unless` 조건으로 인해 2개 항목이 캐시되지 않는다는 사실의 직접적인 증거입니다."

---

## 6. `readOnly` 트랜잭션 + REQUIRES_NEW Provisioner 패턴

### `readOnly = true`가 하는 것

```java
@Transactional(readOnly = true)
public HomeTodayResponse query() { ... }
```

- **Hibernate flush 생략**: readOnly 트랜잭션에서 Hibernate는 flush(dirty check → DB 반영)를 실행하지 않는다. 조회만 하므로 변경 감지가 필요 없다.
- **커넥션 힌트**: Spring이 DataSource에 readOnly 힌트를 전달한다. Read Replica로 라우팅하는 인프라가 있으면 자동으로 읽기 전용 DB로 보낸다.
- **Lock 최적화**: MySQL InnoDB 기준으로 readOnly 트랜잭션은 트랜잭션 ID 할당을 생략할 수 있어 내부 오버헤드가 줄어든다.

### 왜 REQUIRES_NEW Provisioner가 필요한가

`query()`가 `readOnly = true`인데 내부에서 **쓰기(INSERT)가 발생하는 경우**가 있다.

```
query() → readOnly 트랜잭션 시작
  ↓
TodayDailyPlanProvisioner: 오늘 DailyPlan이 없으면 INSERT
  → readOnly 트랜잭션 안에서 INSERT → 오류 또는 flush 안 됨
```

해결: 쓰기 작업을 `REQUIRES_NEW`로 **부모 트랜잭션과 완전히 분리된 새 트랜잭션**에서 실행.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public DailyPlan provideToday(UserId userId, LocalDate date) {
    try {
        DailyPlan plan = DailyPlan.create(userId, date);
        return dailyPlanRepository.save(plan);
    } catch (DataIntegrityViolationException e) {
        // 동시 요청이 같은 plan을 동시에 INSERT → unique 위반
        // REQUIRES_NEW 덕분에 이 트랜잭션만 롤백, 부모 트랜잭션은 살아있음
        return dailyPlanRepository.findByUserIdAndPlanDate(userId, date).orElseThrow();
    }
}
```

REQUIRES_NEW의 핵심: 자식 트랜잭션이 롤백해도 부모 readOnly 트랜잭션이 영향받지 않는다.

### 동시성 Race Condition 수정

최초 로그인 사용자가 `/home/today`를 **동시에** 여러 번 호출할 때:

```
요청 A: DailyPlan 없음 확인 → INSERT 준비
요청 B: DailyPlan 없음 확인 → INSERT 준비
요청 A: INSERT 성공
요청 B: INSERT 실패 → DataIntegrityViolationException (unique 위반)
```

REQUIRES_NEW + catch 패턴으로 요청 B는 예외를 잡아 SELECT로 fallback → 정상 응답.
이것이 없으면 최초 진입 시 500 에러가 발생할 수 있다.

### 그런데 성능 효과는 없었다

readOnly 전환으로 flush ~8.5ms가 절약될 것을 기대했다. Gatling 측정 결과(n=1,385):

```
Run 2: p95 77ms (readOnly 적용 전)
Run 3: p95 78ms (readOnly 적용 후)
```

**효과 없음.** 이유:
1. 8.5ms는 n=1 샘플의 노이즈가 포함된 값
2. 14개 엔티티의 dirty-check 실제 비용은 무시할 수준 (엔티티가 작고 변경 없음)
3. readOnly flush 스킵은 대용량 엔티티 그래프가 있을 때 효과가 크다

이 변경은 성능 최적화가 아니라 **의미론적 정확성**과 **동시성 안전성** 이유로 유지한다.

---

## 7. 캐시는 "빠르게 만드는 도구"가 아니다 — 핵심 교훈

### 캐시의 올바른 역할 정의

**캐시는 응답 시간을 낮추는 것이 목적이 아니다. DB에 가해지는 부하를 흡수하는 것이 목적이다.**

두 문장의 차이:

| 잘못된 이해 | 올바른 이해 |
| --- | --- |
| 캐시 = 빠른 조회 | 캐시 = DB 부하 완충재 |
| 캐시를 붙이면 응답이 빨라진다 | DB가 압박받을 때 캐시가 이득을 낸다 |
| 성능 최적화 = 캐시 도입 | DB가 병목이 아니면 캐시 이득 없음 |

### 언제 캐시가 실질 이득을 내는가

```
DB가 병목인 상황:
  요청 → DB → 응답  (DB가 느림 or 과부하)
  캐시 → 요청 → Redis Hit → 응답  (DB 왕복 생략)

DB가 여유로운 상황:
  요청 → DB → 응답  (DB가 충분히 빠름)
  캐시 → 요청 → Redis Get(miss) → DB → Redis Set → 응답  (더 느림)
```

**DB가 압박받지 않는 상황에서 캐시는 DB 쿼리를 Redis 왕복 + AOP 오버헤드로 교체할 뿐이다.**

### 이 서비스의 경우

- DAU 150, Peak 10 TPS, 요청당 8개 쿼리
- 10 TPS = 초당 80개 DB 쿼리 (10 × 8)
- MySQL은 단일 인스턴스에서 수만 QPS를 처리할 수 있다
- **DB는 전혀 압박받지 않는다**
- 따라서 캐시의 "DB 부하 흡수" 기능이 의미 없다

### 캐시가 이득을 내려면 얼마나 커야 하나

이 서비스 기준 추정:

```
캐시가 DB 쿼리 1개(notification-preference, ~3ms)를 절약

캐시 비용:
  Redis GET × 3회 × 10 TPS = 30 ops/s
  AOP 오버헤드 × 3 × 10 TPS = 30 호출/s

DB 쿼리 3개 × 10 TPS = 30 DB 쿼리/s
MySQL idle capacity: ~10,000 QPS 이상 여유

→ DB가 실제로 압박받는 임계는 수천 TPS 이상
  현재 10 TPS와 수천 TPS 사이에서 캐시 이득이 발생하기 시작
```

### 면접에서 핵심 한 문장으로 설명하는 법

> "캐시는 DB가 병목일 때 부하를 흡수하는 도구입니다. DB가 충분히 여유로운 규모에서는 Redis 왕복과 AOP 오버헤드만 추가되어 오히려 느려질 수 있습니다. 측정 결과 캐시 ON p95 74ms, 캐시 OFF p95 66ms로 캐시가 오히려 느렸고, 이것이 Redis 캐시를 제거한 직접적인 근거입니다."

---

## 8. 수치 총정리 — 면접 암기용

### 성능 수치

| 구간 | p95 변화 | 원인 |
| --- | --- | --- |
| Run 1 → Run 2 | 105ms → 77ms (-27%) | `@Transactional` 통합 + SQL 로깅 OFF |
| Run 2 → Run 3 | 77ms → 78ms (±0) | readOnly 전환 — 효과 없음 |
| 캐시 ON vs OFF | 74ms vs 66–77ms | 캐시 ON이 동등하거나 더 느림 |

### 캐시 제거 수치

| 항목 | 수치 |
| --- | --- |
| 삭제된 코드 | 111줄, 13개 파일 |
| 완전 삭제 파일 | 2개 (HomeCacheKey, HomeCacheNames) |
| 제거된 어노테이션 | 10개 (@Cacheable ×3, @CacheEvict ×6, @CachePut ×1) |
| 제거된 Evict 경로 | 6개 서비스 |
| Redis 명령어 절약 | 2,400회/일, 30 ops/s(피크) |
| Redis 메모리 절약 | 102–246 KB/DAU 150 |

### 프로파일링 수치

| 항목 | 수치 |
| --- | --- |
| 요청당 DB 쿼리 | 8개 |
| JDBC Connection (최적화 전) | ~7개 |
| JDBC Connection (최적화 후) | 1개 |
| DB/Hibernate 합계 | ~43 ms |
| 나머지 (필터·직렬화) | ~13 ms |
| N+1 쿼리 | 없음 (fetch join 확인) |

### 예상 면접 질문과 답변 요점

**Q. 왜 캐시를 도입했다가 제거했나요?**
> 성능 개선을 위해 도입했으나, 실제로 측정해보니 이 규모(DAU 150, 10 TPS)에서 DB가 전혀 압박받지 않아 캐시 이득이 없었습니다. A/B 테스트 결과 캐시 ON p95(74ms)가 캐시 OFF(66ms)보다 오히려 느렸습니다.

**Q. 왜 캐시 ON이 더 느린가요?**
> `unless="#result==null"` 조건으로 null 결과가 캐시에 저장되지 않아, 매 요청마다 Redis GET miss 후 DB 조회가 발생했습니다. AOP 오버헤드가 절약된 DB 쿼리 비용보다 컸습니다.

**Q. @Transactional 하나가 왜 성능에 영향을 주나요?**
> 상위 메서드에 @Transactional이 없으면 하위 서비스가 각자 REQUIRED로 새 트랜잭션을 열어 JDBC Connection을 7번 획득·반납합니다. 단일 @Transactional로 묶으면 1개 Connection으로 8개 조회가 처리됩니다.

**Q. readOnly 트랜잭션 안에서 INSERT가 필요한 경우 어떻게 처리했나요?**
> REQUIRES_NEW 전파 속성의 Provisioner 클래스로 분리했습니다. DataIntegrityViolationException을 catch해 동시 요청의 unique 충돌도 처리합니다.

**Q. 이 작업의 핵심 교훈은 무엇인가요?**
> 캐시는 "빠르게 만드는 도구"가 아니라 "DB 부하를 흡수하는 도구"입니다. DB가 여유로운 규모에서는 캐시가 복잡성 비용만 남깁니다. 추측이 아니라 측정이 판단의 근거여야 합니다.

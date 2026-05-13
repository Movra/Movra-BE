# 홈 화면 조회 성능 최적화 — `GET /home/today`

> `QueryHomeTodayService.query()`의 직렬 DB 호출 10여 회를 줄이고, 변경이 적은 응답 구성요소에 Redis 캐시를 도입하여 응답 시간을 단축한 작업 기록.

## 목차

- [요약](#요약)
- [Before / After 성능 비교](#before--after-성능-비교)
- [최적화 전 진단](#최적화-전-진단)
- [적용한 최적화](#적용한-최적화)
  - [1. 조회 시점의 의도된 Exception을 Optional 반환으로 전환](#1-조회-시점의-의도된-exception을-optional-반환으로-전환)
  - [2. 변경이 잦지 않은 조회에 Redis 캐시 도입](#2-변경이-잦지-않은-조회에-redis-캐시-도입)
  - [3. 중복 쿼리를 단일 쿼리로 통합](#3-중복-쿼리를-단일-쿼리로-통합)
- [측정 방법](#측정-방법)
- [고려사항과 잔존 리스크](#고려사항과-잔존-리스크)

---

## 요약

| 항목 | 값 |
| --- | --- |
| 대상 | `GET /home/today` (앱 진입 시 가장 많이 호출되는 엔드포인트) |
| 측정 도구 | Gatling 3.13.5 (Closed model, 1 virtual user × 30회 순차 호출 + warmup 3회) |
| 측정 환경 | 로컬, Spring Boot 3.5.11, MySQL, Redis, Virtual Threads on |
| 핵심 개선 | **p95 -49.5%, Mean -35.9%, p99 -56.8%** |

---

## Before / After 성능 비교

### Before (Baseline)

| 지표 | 값 |
| --- | --- |
| 성공률 | 100% (33/33) |
| Min | 114 ms |
| Mean | 248 ms |
| p50 | 159 ms |
| p75 | 177 ms |
| p95 | 404 ms |
| p99 | 2,790 ms *(cold start 첫 호출 이상치)* |
| Max | 2,790 ms |
| 분포 | t < 800ms : 96.97% |
| 처리량 | 1.27 rps |

### After (최적화 후)

| 지표 | 값 |
| --- | --- |
| 성공률 | 100% (33/33) |
| Min | 71 ms |
| Mean | 159 ms |
| p50 | 125 ms |
| p75 | 148 ms |
| p95 | 204 ms |
| p99 | 1,206 ms |
| Max | 1,206 ms |
| 분포 | t < 800ms : 96.97% |
| 처리량 | 1.43 rps |

### 비교

| 지표 | Before | After | 개선 |
| --- | ---: | ---: | --- |
| Min | 114 ms | **71 ms** | **-37.7%** |
| Mean | 248 ms | **159 ms** | **-35.9%** |
| p50 | 159 ms | **125 ms** | **-21.4%** |
| p75 | 177 ms | **148 ms** | -16.4% |
| **p95** | 404 ms | **204 ms** | **-49.5%** |
| p99 | 2,790 ms | **1,206 ms** | **-56.8%** |
| Max | 2,790 ms | **1,206 ms** | **-56.8%** |
| Throughput | 1.27 rps | **1.43 rps** | +12.6% |

---

## 최적화 전 진단

`QueryHomeTodayService.query()` 한 번이 단순 합산으로도 **DB 왕복 10회 이상**을 일으키고 있었다. 호출 순서를 따라가 보면 다음과 같다.

| 단계 | 호출 | 비고 |
| --- | --- | --- |
| 1 | `DailyPlan` 조회 | 없으면 생성 |
| 2 | `Task`(top picks) 조회 | DailyPlan과 별도 트랜잭션·별도 서비스에서 다시 조회 |
| 3 | `Timetable` 조회 | DailyPlan과 별도 트랜잭션·별도 서비스에서 다시 조회 |
| 4 | `FutureVision` 조회 | 등록 안 했으면 `FutureVisionNotFoundException` |
| 5 | `ExamSchedule` 조회 (next) | 없으면 `ExamScheduleNotFoundException` |
| 6 | `NotificationPreference` 조회 | 없으면 default 생성 후 저장 |
| 7 | `AccountabilityRelation` (subject) | `findBySubjectUserId` |
| 8 | `AccountabilityRelation` (watcher) | `findByWatcherUserId` |
| 9 | `ActivationFunnel` 조회 | focus card 노출 판단 |

**병목의 본질은 “쿼리 수 자체”가 아니라 구조였다.**

- `DailyPlan`을 여러 하위 서비스(`QueryTodayTopPicksService`, `QueryTodayTimetableService`)가 **각자 다시 조회·검증**하고 있어, 같은 `DailyPlan`이 사실상 여러 번 로드됨
- 홈에서는 **사용자 본인이 아직 등록하지 않은 도메인(비전·시험·알림 설정)을 정상 케이스로 다뤄야 함에도, 조회 서비스가 `*NotFoundException`을 던지는 경로**가 메인이었음 → 호출자(`QueryHomeTodayService`)에서 try/catch 또는 별도 분기로 받아야 했고, 예외 인스턴스 생성·스택 트레이스 비용도 누적
- `AccountabilityRelation`은 **본질적으로 한 사용자에 대한 한 쿼리로 충분**한데, "내가 subject인 관계"와 "내가 watcher인 관계"를 두 번에 나눠서 조회

> 이 진단을 바탕으로 다음 세 가지 방향을 정했다.
>
> 1. **조회 시점의 의도된 Exception을 NULL/Optional 반환으로 바꾸기**
> 2. **변경이 잘 되지 않는 기능은 Cache 도입**
> 3. **중복 쿼리 하나로 줄이기**

---

## 적용한 최적화

### 1. 조회 시점의 의도된 Exception을 Optional 반환으로 전환

홈 화면에서의 "비전 미등록", "시험 일정 미등록"은 **버그가 아니라 정상 상태**다. 그럼에도 기존 조회 서비스는 단건 조회 실패 시 도메인 예외를 던졌고, `QueryHomeTodayService`는 이를 회피하기 위해 우회 경로를 가져야 했다.

홈 전용 진입점을 새로 만들어 `Optional<...>`을 반환하도록 변경했다. 기존의 예외 던지는 API(`query()`, `queryNext()`)는 단건 화면용으로 그대로 유지한다.

**`QueryFutureVisionService`**
```java
// 단건 화면용 — 미등록이면 예외
@Transactional(readOnly = true)
public FutureVisionResponse query() {
    return FutureVisionResponse.from(getFutureVisionOrThrow());
}

// 홈용 — 미등록은 정상 상태이므로 Optional
@Cacheable(
        cacheNames = HomeCacheNames.FUTURE_VISION,
        key = "@homeCacheKey.currentUserId()"
)
@Transactional(readOnly = true)
public Optional<FutureVisionResponse> findForHome() {
    return futureVisionRepository.findByUserId(currentUserId())
            .map(FutureVisionResponse::from);
}
```

**`QueryExamScheduleService`**
```java
// 단건 화면용 — 미등록이면 예외
@Transactional(readOnly = true)
public ExamScheduleResponse queryNext() { ... orElseThrow(ExamScheduleNotFoundException::new); }

// 홈용 — Optional + 캐시
@Cacheable(
        cacheNames = HomeCacheNames.NEXT_EXAM_SCHEDULE,
        key = "@homeCacheKey.currentUserIdToday()"
)
@Transactional(readOnly = true)
public Optional<ExamScheduleResponse> findNextForHome() {
    UserId userId = currentUserQuery.currentUser().userId();
    LocalDate today = LocalDate.now(clock);
    return examScheduleRepository
            .findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today)
            .map(es -> ExamScheduleResponse.from(es, today));
}
```

**호출부도 깔끔해졌다.**
```java
// QueryHomeTodayService
private FutureVisionResponse queryFutureVision() {
    return queryFutureVisionService.findForHome().orElse(null);
}

private ExamScheduleResponse queryNextExamSchedule() {
    return queryExamScheduleService.findNextForHome().orElse(null);
}
```

**왜 효과가 있는가**
- 정상 흐름에서 발생하던 예외가 사라지면서 스택 트레이스 채움 비용 제거 (`Throwable.fillInStackTrace()`는 JVM에서 의외로 무거움)
- 호출부의 try/catch / 분기 로직 제거 — 코드 가독성도 회복
- 이후 캐시·트랜잭션 어노테이션을 같은 메서드에 안전하게 붙일 수 있는 기반이 됨

---

### 2. 변경이 잦지 않은 조회에 Redis 캐시 도입

홈 응답 구성요소 중 **사용자 행동 한 번당 변경 빈도가 매우 낮은** 세 가지를 캐시 대상으로 골랐다.

| 캐시 이름 | 대상 | TTL | 키 |
| --- | --- | --- | --- |
| `home:future-vision` | 미래 비전 | 12h | `currentUserId` |
| `home:notification-preference` | 알림 환경설정 | 12h | `currentUserId` |
| `home:next-exam-schedule` | 다음 시험 일정 | 6h | `currentUserId:yyyy-MM-dd` |

`next-exam-schedule`만 키에 날짜를 포함했다. **다음 시험은 날짜가 바뀌면 결과가 달라질 수 있기 때문**(어제까지 다음 시험이었던 항목이 오늘 지나가는 케이스). 매일 0시에 자연스레 새 키로 빠져나가도록 설계했다.

**캐시 키 컴포넌트** (`config/cache/HomeCacheKey.java`)
```java
@Component
@RequiredArgsConstructor
public class HomeCacheKey {
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    public String currentUserId() {
        return currentUserQuery.currentUser().userId().id().toString();
    }

    public String currentUserIdToday() {
        return currentUserId() + ":" + LocalDate.now(clock);
    }
}
```

`@Cacheable(key = "@homeCacheKey.currentUserId()")`처럼 SpEL로 빈을 참조해, 인자가 없는 메서드(`findForHome()`)에서도 사용자별 캐시 키를 만들 수 있다. `Clock` 주입 덕분에 테스트에서 키를 결정적으로 만들 수 있다.

**Redis 캐시 설정** (`config/cache/RedisCacheConfig.java`)
```java
RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(... new StringRedisSerializer())
        .serializeValuesWith(... jsonSerializer)
        .disableCachingNullValues()       // null 응답은 캐시하지 않음
        .entryTtl(Duration.ofMinutes(30));

return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(Map.of(
                HomeCacheNames.FUTURE_VISION,          defaultConfig.entryTtl(Duration.ofHours(12)),
                HomeCacheNames.NOTIFICATION_PREFERENCE, defaultConfig.entryTtl(Duration.ofHours(12)),
                HomeCacheNames.NEXT_EXAM_SCHEDULE,     defaultConfig.entryTtl(Duration.ofHours(6))
        ))
        .build();
```

- `GenericJackson2JsonRedisSerializer` + `activateDefaultTyping`으로 폴리모픽 응답(`Optional`, sealed 응답 계층 등)도 안전하게 역직렬화
- `BasicPolymorphicTypeValidator`로 `com.example.movra` 패키지만 허용 — Jackson default typing의 RCE 리스크 차단
- `disableCachingNullValues()` — 미등록 사용자의 빈 응답을 Redis에 저장하지 않아 메모리 낭비 방지

**캐시 무효화** — 변경 서비스마다 `@CacheEvict` / `@CachePut`을 빠짐없이 부착했다.

| 변경 동작 | 어노테이션 | 캐시 |
| --- | --- | --- |
| `CreateFutureVisionService.create` | `@CacheEvict` | `home:future-vision` |
| `UpdateWeeklyVisionService.update` | `@CacheEvict` | `home:future-vision` |
| `UpdateYearlyVisionService.update` | `@CacheEvict` | `home:future-vision` |
| `CreateExamScheduleService.create` | `@CacheEvict` | `home:next-exam-schedule` |
| `UpdateExamScheduleService.update` | `@CacheEvict` | `home:next-exam-schedule` |
| `DeleteExamScheduleService.delete` | `@CacheEvict` | `home:next-exam-schedule` |
| `UpdateNotificationPreferenceService.update` | `@CachePut` | `home:notification-preference` |

`@CachePut`을 쓴 곳은 알림 환경설정만이다. 알림 설정 변경은 **응답으로 바뀐 상태를 즉시 반환**하므로 evict 후 다음 호출에 다시 채우기보다, 변경된 값을 그대로 캐시에 올리는 쪽이 자연스럽다.

**`spring.cache.type=redis`** 명시(`application.yml`) — 의도하지 않은 in-memory fallback 차단.

---

### 3. 중복 쿼리를 단일 쿼리로 통합

#### (a) `AccountabilityRelation` — 두 번의 조회를 한 번으로

**Before** — `QueryHomeTodayService`가 친구 책임감(accountability) 상태를 만들기 위해 두 번 호출
```java
relationRepository.findBySubjectUserId(userId)  // 내가 subject 인가
relationRepository.findByWatcherUserId(userId)  // 내가 watcher 인가
```

**After** — 한 번의 쿼리로 양쪽 후보를 모두 가져와 메모리에서 분류
```java
// AccountabilityRelationRepository
List<AccountabilityRelation> findAllBySubjectUserIdOrWatcherUserId(UserId subjectUserId, UserId watcherUserId);

// QueryHomeTodayService
List<AccountabilityRelation> relations =
        accountabilityRelationRepository.findAllBySubjectUserIdOrWatcherUserId(userId, userId);

Optional<AccountabilityRelation> subjectRelation = relations.stream()
        .filter(r -> userId.equals(r.getSubjectUserId()))
        .findFirst();

Optional<AccountabilityRelation> watcherRelation = relations.stream()
        .filter(r -> userId.equals(r.getWatcherUserId()))
        .findFirst();
```

> 한 사용자가 갖는 관계 수는 매우 작아 (보통 0~2건), 메모리 분류 비용은 무시할 수 있다. **DB 왕복 -1회는 그대로 절감**.

#### (b) `DailyPlan` + `Task` + `Timetable` — 중복 로드 제거

기존에는 `top picks`, `timetable`이 각자의 조회 서비스에서 다시 `DailyPlan`을 끌어와 검증·매핑하고 있었다. 홈 진입 시 **같은 `DailyPlan`을 사실상 3번 로드**하는 셈이었다.

홈 전용 통합 진입점 `QueryTodayPlanningOverviewService.query()`를 두고, `DailyPlan`을 한 번만 로드한 뒤 거기서 파생되는 응답(top picks, timetable)을 모두 만든다.

```java
@Service
@RequiredArgsConstructor
public class QueryTodayPlanningOverviewService {

    @Transactional
    public TodayPlanningOverviewResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate today = LocalDate.now(clock);
        DailyPlan dailyPlan = findOrCreateToday(userId, today);

        return TodayPlanningOverviewResponse.builder()
                .dailyPlanId(dailyPlan.getDailyPlanId().id())
                .targetDate(dailyPlan.getPlanDate())
                .topPicks(topPicks(dailyPlan))   // 메모리에서 필터링
                .timetable(timetable(dailyPlan)) // dailyPlanId로 단발 조회
                .build();
    }

    private List<TopPicksResponse> topPicks(DailyPlan dailyPlan) {
        return dailyPlan.getTasks().stream()
                .filter(Task::isTopPicked)
                .map(TopPicksResponse::from)
                .toList();
    }
}
```

`DailyPlan` 한 번을 가능한 한 풍부하게 가져오기 위해 fetch join 쿼리를 추가했다 — **N+1 차단**이 핵심.

```java
// DailyPlanRepository
@Query("""
        SELECT DISTINCT dp
        FROM DailyPlan dp
        LEFT JOIN FETCH dp.tasks task
        LEFT JOIN FETCH task.topPickDetail
        WHERE dp.userId = :userId
          AND dp.planDate = :planDate
        """)
Optional<DailyPlan> findByUserIdAndPlanDateWithTasks(...);

// TimetableRepository
@Query("""
        SELECT DISTINCT t
        FROM Timetable t
        LEFT JOIN FETCH t.slots
        WHERE t.dailyPlanId = :dailyPlanId
        """)
Optional<Timetable> findByDailyPlanIdWithSlots(...);
```

#### (c) Duplicate key 경합 처리

`DailyPlan`은 첫 진입 시 자동 생성된다. 동시 요청 두 건이 모두 "없음 → 생성"으로 진입하면 한쪽은 unique constraint 위반으로 실패한다. 이전에는 이를 그대로 노출했지만, 이번에 **`DataIntegrityViolationUtils.isDuplicateKeyViolation`**으로 정확한 케이스만 잡아 재조회로 복구하도록 했다.

```java
private DailyPlan createOrLoadToday(UserId userId, LocalDate today) {
    try {
        return dailyPlanRepository.saveAndFlush(DailyPlan.create(userId, today));
    } catch (DataIntegrityViolationException e) {
        if (!DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
            throw e;  // 무관한 위반은 그대로 전파
        }
        return dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, today)
                .orElseThrow(DailyPlanAlreadyExistsException::new);
    }
}
```

---

## 측정 방법

**Gatling 시뮬레이션** (`src/gatling/java/HomeTodayBaselineSimulation.java`)

- Closed model: 1 virtual user가 워밍업 3회 + 측정 30회를 순차 호출
- 매 요청 사이 500ms pause — 단일 요청의 순수 처리 비용 측정 목적
- HTTP 200 응답만 OK로 카운트
- 어설션: p95 < 1000ms, p99 < 2000ms, 실패율 < 1%

**실행 명령**
```bash
./gradlew gatlingRun \
    -DauthToken="<JWT>" \
    -DbaseUrl=http://localhost:8080 \
    -Diterations=30 -DpauseMs=500 -DwarmupIterations=3
```

리포트는 `build/reports/gatling/hometodaybaselinesimulation-<timestamp>/index.html`에 생성된다.

---

## 고려사항과 잔존 리스크

### 캐시 무효화 누락 시 stale 응답

`@CacheEvict`를 변경 서비스 어디에 빠뜨리면 사용자는 자신이 방금 수정한 결과를 보지 못한다. 본 작업에서는 변경 서비스 7개 모두 매핑했지만, **향후 같은 도메인에 새 변경 서비스를 추가할 때 빠뜨릴 가능성**이 있다. 도메인 이벤트 기반 invalidation으로 옮기는 것이 다음 단계.

### 캐시 herd / cold call

p99의 1,206ms는 **캐시 미스(콜드 첫 호출)** 비용. TTL 만료 직후 동시 트래픽이 몰리면 다시 튈 수 있다.
- 1차 대응: TTL 차등(6h / 12h)으로 동시 만료 최소화
- 다음 단계: cache stampede protection (`@Cacheable(sync = true)` 또는 Redis 분산 락) 검토

### 동시성 한계는 별도 검증 필요

이번 측정은 단일 사용자 순차(closed model). **동시 트래픽 한계**는 별도로 검증해야 한다. 이전 open-model 3 RPS에서 실패율 90%+가 관측됐는데, 그 원인이 HikariCP 풀 고갈인지 다른 병목인지 추후 확인 필요.

### Virtual Threads + JPA pinning

`spring.threads.virtual.enabled=true` 상태. JPA/JDBC `synchronized`로 인한 carrier thread pinning은 부하 상황에서 다시 검증할 것.

---

## 변경 파일 인덱스

**신규**
- `src/main/java/com/example/movra/bc/home/today/application/service/QueryHomeTodayService.java`
- `src/main/java/com/example/movra/bc/home/today/application/service/dto/response/HomeTodayResponse.java`
- `src/main/java/com/example/movra/bc/home/today/application/service/dto/response/FriendAccountabilityStatusResponse.java`
- `src/main/java/com/example/movra/bc/home/today/presentation/HomeController.java`
- `src/main/java/com/example/movra/bc/planning/daily_plan/application/service/daily_plan/QueryTodayPlanningOverviewService.java`
- `src/main/java/com/example/movra/bc/planning/daily_plan/application/service/daily_plan/dto/response/TodayPlanningOverviewResponse.java`
- `src/main/java/com/example/movra/config/cache/HomeCacheKey.java`
- `src/main/java/com/example/movra/config/cache/HomeCacheNames.java`
- `src/main/java/com/example/movra/config/cache/RedisCacheConfig.java`
- `src/gatling/java/HomeTodayBaselineSimulation.java`

**수정**
- `bc/visioning/future_vision/application/service/*` — `findForHome()` 추가, 변경 서비스에 `@CacheEvict`
- `bc/planning/exam_schedule/application/service/*` — `findNextForHome()` 추가, 변경 서비스에 `@CacheEvict`
- `bc/notification/application/service/*` — `@Cacheable` / `@CachePut`
- `bc/accountability/accountability_relation/domain/repository/AccountabilityRelationRepository.java` — `findAllBySubjectUserIdOrWatcherUserId` 추가
- `bc/planning/daily_plan/domain/repository/DailyPlanRepository.java` — `findByUserIdAndPlanDateWithTasks` fetch join
- `bc/planning/timetable/domain/repository/TimetableRepository.java` — `findByDailyPlanIdWithSlots` fetch join
- `bc/analytics/activation_funnel/domain/ActivationFunnel.java` — `isFocusTimingCardAvailable(Clock)` 도메인 메서드
- `src/main/resources/application.yml` — `spring.cache.type=redis`

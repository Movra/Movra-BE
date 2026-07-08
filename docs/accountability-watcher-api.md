# Accountability Watcher API Response Contract

이 문서는 watcher가 친구 데이터를 조회하는 기존 Accountability API의 응답 JSON을 명시한다. 새 API나 새 DTO를 추가하지 않고, 이미 구현된 `WatcherQueryController`와 summary view record의 Jackson 직렬화 형태를 문서화한다.

## 구현 위치

- `src/main/java/com/example/movra/bc/accountability/accountability_relation/presentation/WatcherQueryController.java`
- `src/main/java/com/example/movra/bc/accountability/accountability_relation/application/service/query/QueryWatcherOverviewService.java`
- `src/main/java/com/example/movra/bc/accountability/accountability_relation/application/service/dto/response/WatcherOverviewResponse.java`
- `src/main/java/com/example/movra/bc/focus/focus_session/application/service/support/dto/DailyFocusSummaryView.java`
- `src/main/java/com/example/movra/bc/focus/focus_session/application/service/support/dto/DailyFocusSummaryItemView.java`
- `src/main/java/com/example/movra/bc/planning/daily_plan/application/service/daily_plan/support/dto/DailyTopPicksSummaryView.java`
- `src/main/java/com/example/movra/bc/planning/daily_plan/application/service/daily_plan/support/dto/DailyTopPicksSummaryItemView.java`
- `src/main/java/com/example/movra/bc/planning/timetable/application/service/support/dto/DailyTimetableSummaryView.java`
- `src/main/java/com/example/movra/bc/planning/timetable/application/service/support/dto/DailyTimetableSummaryItemView.java`
- `src/test/java/com/example/movra/presentation/accountability/accountability_relation/WatcherQueryControllerTest.java`

## 공통 규칙

- `POST /accountability-relations/join`은 연결된 관계의 `FriendAccountabilityRelationResponse`를 반환하므로 watcher는 join 직후 `allowedTargets`를 확인할 수 있다.
- `GET /accountability-relations/watcher/overview`는 관계, 허용 대상, 허용된 감시 내용을 한 번에 반환한다.
- overview와 단일 날짜 조회는 마감 스냅샷을 우선 사용하고, 스냅샷이 없으면 현재 원본 데이터에서 live view를 구성한다.
- 단일 날짜 조회는 데이터가 있으면 `200 OK`와 객체를 반환한다.
- 단일 날짜 조회는 데이터가 없으면 `204 No Content`를 반환한다.
- 기간 조회는 마감 스냅샷 범위만 조회하며 항상 `200 OK`와 배열을 반환한다. 데이터가 없으면 빈 배열 `[]`이다.
- `userId`는 `UserId(UUID id)` record의 Jackson 기본 직렬화 형태이므로 문자열이 아니라 `{ "id": "uuid" }` 객체다.
- `Instant`는 ISO-8601 UTC 문자열, `LocalDate`는 `yyyy-MM-dd`, `LocalTime`은 `HH:mm:ss` 형태다.

## Overview

```http
GET /accountability-relations/watcher/overview?date=2026-04-20
Authorization: Bearer {token}
```

```json
{
  "relation": {
    "accountabilityRelationId": "11111111-1111-1111-1111-111111111111",
    "subjectUserId": "550e8400-e29b-41d4-a716-446655440000",
    "watcherUserId": "660e8400-e29b-41d4-a716-446655440000",
    "watcherConnected": true,
    "allowedTargets": [
      "TOP_PICKS",
      "TIMETABLE_TASK"
    ]
  },
  "date": "2026-04-20",
  "focusSessions": null,
  "topPicks": {
    "userId": {
      "id": "550e8400-e29b-41d4-a716-446655440000"
    },
    "date": "2026-04-20",
    "totalCount": 1,
    "completedCount": 1,
    "items": [
      {
        "content": "수학 문제집",
        "completed": true,
        "estimatedMinutes": 30,
        "memo": "2단원",
        "displayOrder": 1
      }
    ]
  },
  "timetableTasks": {
    "userId": {
      "id": "550e8400-e29b-41d4-a716-446655440000"
    },
    "date": "2026-04-20",
    "totalCount": 1,
    "completedCount": 0,
    "items": [
      {
        "contentSnapshot": "영어 단어",
        "completedSnapshot": false,
        "startTimeSnapshot": "09:00:00",
        "endTimeSnapshot": "10:00:00",
        "topPickSnapshot": true,
        "displayOrder": 1
      }
    ]
  }
}
```

`allowedTargets`에 없는 항목이나 데이터가 없는 항목은 `null`이다. 위 예시는 `FOCUS_SESSION`이 허용되지 않아 `focusSessions`가 `null`인 경우다.

## Focus Sessions

### 단일 날짜 조회

```http
GET /accountability-relations/watcher/focus-sessions?date=2026-04-20
Authorization: Bearer {token}
```

```json
{
  "userId": {
    "id": "550e8400-e29b-41d4-a716-446655440000"
  },
  "date": "2026-04-20",
  "totalSeconds": 3600,
  "sessionCount": 2,
  "items": [
    {
      "startedAtSnapshot": "2026-04-20T01:00:00Z",
      "endedAtSnapshot": "2026-04-20T01:30:00Z",
      "recordedDurationSecondsSnapshot": 1800,
      "overlapStartedAt": "2026-04-20T01:00:00Z",
      "overlapEndedAt": "2026-04-20T01:30:00Z",
      "overlapSeconds": 1800,
      "displayOrder": 0
    }
  ]
}
```

### 기간 조회

```http
GET /accountability-relations/watcher/focus-sessions/range?from=2026-04-20&to=2026-04-24
Authorization: Bearer {token}
```

Response: `DailyFocusSummaryView[]`  
배열의 각 요소는 단일 날짜 조회 응답 객체와 동일한 필드를 가진다.

## Top Picks

### 단일 날짜 조회

```http
GET /accountability-relations/watcher/top-picks?date=2026-04-20
Authorization: Bearer {token}
```

```json
{
  "userId": {
    "id": "550e8400-e29b-41d4-a716-446655440000"
  },
  "date": "2026-04-20",
  "totalCount": 3,
  "completedCount": 2,
  "items": [
    {
      "content": "공부하기",
      "completed": true,
      "estimatedMinutes": 30,
      "memo": "열심히",
      "displayOrder": 0
    }
  ]
}
```

### 기간 조회

```http
GET /accountability-relations/watcher/top-picks/range?from=2026-04-20&to=2026-04-24
Authorization: Bearer {token}
```

Response: `DailyTopPicksSummaryView[]`  
배열의 각 요소는 단일 날짜 조회 응답 객체와 동일한 필드를 가진다.

## Timetable Tasks

### 단일 날짜 조회

```http
GET /accountability-relations/watcher/timetable-tasks?date=2026-04-20
Authorization: Bearer {token}
```

```json
{
  "userId": {
    "id": "550e8400-e29b-41d4-a716-446655440000"
  },
  "date": "2026-04-20",
  "totalCount": 4,
  "completedCount": 3,
  "items": [
    {
      "contentSnapshot": "수학 공부",
      "completedSnapshot": true,
      "startTimeSnapshot": "09:00:00",
      "endTimeSnapshot": "10:00:00",
      "topPickSnapshot": true,
      "displayOrder": 0
    }
  ]
}
```

### 기간 조회

```http
GET /accountability-relations/watcher/timetable-tasks/range?from=2026-04-20&to=2026-04-24
Authorization: Bearer {token}
```

Response: `DailyTimetableSummaryView[]`  
배열의 각 요소는 단일 날짜 조회 응답 객체와 동일한 필드를 가진다.

## 에러

- `MONITORING_TARGET_NOT_ALLOWED` (403): 허용되지 않은 모니터링 대상
- `INVALID_DATE_RANGE` (400): 유효하지 않은 날짜 범위


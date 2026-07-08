# MORVA 서버 API 명세서

> 기준일: 2026-06-22  
> 기준 코드: `src/main/java`의 Controller, Request/Response DTO, Security, Exception 구현  
> 범위: REST API 104개, STOMP 메시지 API 1개, Spring Security OAuth2 진입점

이 문서는 설계안이 아니라 **현재 코드가 실제로 제공하는 계약**을 정리한다. 서버 주소는 환경별로 다르므로 아래 경로는 모두 Base URL 이후의 상대 경로다.

## 1. 프로젝트 분석 요약

MORVA는 Spring Boot 3.5와 Java 21 기반의 일정·학습 생산성 백엔드다. 구현은 DDD 지향 bounded context와 command/query 분리를 사용하며, 현재 API 표면은 다음 영역으로 나뉜다.

| 영역 | 주요 기능 | API 수 |
|---|---|---:|
| Account | 로컬/OAuth 인증, 토큰 재발급, 내 프로필 | 7 |
| Planning | 일일 계획, 마인드 스윕, 아침 작업, Top Pick, 시험, 시간표 | 31 |
| Focus/Statistics | 집중 세션, 복구 카드, 집중 통계 | 10 |
| Feedback/Visioning | 작은 성과, 일일 회고, 미래 비전 | 15 |
| Study Room | 방, 참여자, 집중/휴식, 채팅 | 12 REST + 1 STOMP |
| Accountability | 친구 감시 관계 및 허용 데이터 조회 | 15 |
| Personalization/Insight | 행동 프로필, 조정 제안, AI 인사이트 | 9 |
| Home/Notification/Analytics | 홈 집계, 알림 설정, 행동 이벤트 | 5 |

`CLAUDE.md`의 요약보다 실제 코드에는 Accountability, Analytics, Focus, Home, Insight, Notification, Personalization, Statistics BC가 추가되어 있다. API 소비자는 이 문서를 현재 구현 기준으로 사용해야 한다.

## 2. 공통 계약

### 2.1 인증

- 공개 API: `POST /auth/signup`, `POST /auth/login`, `POST /auth/oauth/complete`, `POST /auth/oauth/profile-setup`, `POST /auth/reissue`, `GET /auth/onboarding-context`, `/ws/**`
- 그 외 REST API는 JWT 인증이 필요하다.
- 예시는 `Authorization: Bearer {accessToken}`을 사용한다. 실제 헤더명과 prefix는 배포 환경의 `HEADER`, `PREFIX` 값으로 정해진다.
- WebSocket HTTP handshake는 공개지만 STOMP `CONNECT` 시 동일한 JWT 헤더가 반드시 필요하다.
- 세션은 stateless이며 CSRF는 비활성화되어 있다.

### 2.2 요청 형식

- 기본 요청/응답: `application/json`
- 파일 요청: `multipart/form-data`
- UUID: 표준 하이픈 포함 UUID 문자열
- `LocalDate`: `yyyy-MM-dd`
- `LocalTime`: 일반 응답은 Jackson 기본 `HH:mm:ss`, 알림 설정은 `HH:mm`
- `Instant`: ISO-8601 UTC 문자열(예: `2026-06-22T03:00:00Z`)
- `LocalDateTime`: timezone 없는 ISO-8601 문자열(예: `2026-06-22T12:00:00`)
- 서버 비즈니스 시간대는 `Asia/Seoul`이다.
- multipart 파일 제한은 파일당 2 MB, 요청 전체 12 MB다.

### 2.3 성공 상태 코드

- 별도 표기가 없는 모든 API는 현재 구현상 `200 OK`다. 생성·수정·삭제 메서드가 body 없이 끝나도 `200`이다.
- `POST /focus-sessions/recovery-card/actions`만 명시적으로 `204 No Content`다.
- Watcher 단일 날짜 조회 3종은 데이터가 없으면 `204 No Content`다.
- Spring OAuth2 흐름은 프론트엔드로 `302 Found` redirect한다.

### 2.4 오류 응답

Controller/서비스에서 처리되는 오류의 공통 body는 다음과 같다.

```json
{
  "httpStatus": "BAD_REQUEST",
  "statusCode": 400,
  "message": "잘못된 요청입니다.",
  "timestamp": "2026-06-22T12:00:00"
}
```

| 상황 | 상태 |
|---|---:|
| JSON, query/path 타입, 필수 파라미터, Bean Validation 실패 | 400 |
| 만료/유효하지 않은 JWT | 401 |
| 권한 또는 공개 범위 위반 | 403 |
| 리소스 없음 | 404 |
| 중복 생성/진행 상태 충돌 | 409 |
| multipart 크기 초과 | 413 |
| 예상하지 못한 서버 오류 | 500 |

주의: 내부 `ErrorCode` enum 이름은 오류 body에 직렬화되지 않는다. 클라이언트가 받는 식별 정보는 현재 `httpStatus`, `statusCode`, `message`, `timestamp`뿐이다. 또한 인증 헤더가 아예 없는 요청은 별도 `AuthenticationEntryPoint`가 없어 위 JSON 형식이 아닌 Spring Security 기본 거부 응답이 될 수 있다.

## 3. REST API

표의 `인증`은 `공개`가 아니면 모두 JWT다. `요청`에는 body와 필수 path/query 파라미터를 함께 표기한다.

### 3.1 Account

| Method | Path | 인증 | 요청 | 응답 | 설명 |
|---|---|---|---|---|---|
| GET | `/auth/onboarding-context` | 공개 | 없음 | `OnboardingContextResponse` | 온보딩에 필요한 컨텍스트 조회 |
| POST | `/auth/signup` | 공개 | multipart `LocalSignupRequest` | body 없음 | 로컬 사용자 가입 |
| POST | `/auth/login` | 공개 | JSON `LocalLoginRequest` | `TokenResponse` | 로컬 로그인 |
| POST | `/auth/oauth/complete` | 공개 | JSON `OauthCompleteRequest` | `OauthCompleteResponse` | OAuth redirect callback code 교환 |
| POST | `/auth/oauth/profile-setup` | 공개 | form/query `pendingToken`, multipart `OauthProfileSetupRequest` | `ProfileSetupResponse` | 신규 OAuth 사용자 프로필 완성 |
| POST | `/auth/reissue` | 공개 | JSON `TokenReissueRequest` | `TokenResponse` | refresh token으로 토큰 재발급 |
| GET | `/users/me` | JWT | 없음 | `ProfileResponse` | 현재 사용자 프로필 조회 |

Spring Security OAuth2 진입점은 `GET /oauth2/authorization/google`, `GET /oauth2/authorization/naver`이며 callback은 `/login/oauth2/code/{provider}`다. 성공 시 서버는 `FRONTEND_URL?code={callbackCode}`로 redirect한다. 프론트엔드는 `POST /auth/oauth/complete`에 이 code를 보내 토큰으로 교환한다. 기존 사용자는 `accessToken`, `refreshToken`, `isProfileCompleted=true`를 받고, 신규 사용자는 `pendingToken`, `isProfileCompleted=false`를 받은 뒤 `POST /auth/oauth/profile-setup`의 multipart form field 또는 query parameter로 `pendingToken`을 보낸다.

### 3.2 Daily Plan / Task

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/daily-plans` | `DailyPlanRequest` | body 없음 | 지정 날짜 일일 계획 생성 |
| GET | `/daily-plans/today` | 없음 | `DailyPlanResponse` | 오늘 계획 조회, 없으면 생성 |
| GET | `/daily-plans?planDate={date}` | query `planDate` | `DailyPlanResponse` | 날짜별 계획 조회 |
| GET | `/daily-plans/{dailyPlanId}/mind-sweeps` | path `dailyPlanId` | `MindSweepResponse[]` | 마인드 스윕 목록 |
| POST | `/daily-plans/{dailyPlanId}/mind-sweeps` | path + `MindSweepRequest` | body 없음 | 마인드 스윕 추가 |
| PUT | `/daily-plans/{dailyPlanId}/mind-sweeps/{taskId}` | path + `MindSweepRequest` | body 없음 | 마인드 스윕 수정 |
| DELETE | `/daily-plans/{dailyPlanId}/mind-sweeps/{taskId}` | path | body 없음 | 마인드 스윕 삭제 |
| PATCH | `/daily-plans/{dailyPlanId}/mind-sweeps/{taskId}/complete` | path | body 없음 | 완료 처리 |
| PATCH | `/daily-plans/{dailyPlanId}/mind-sweeps/{taskId}/uncomplete` | path | body 없음 | 완료 취소 |
| GET | `/morning-tasks?targetDate={date}` | query `targetDate` | `MindSweepResponse[]` | 날짜별 아침 작업 목록 |
| POST | `/morning-tasks?targetDate={date}` | query + `MindSweepRequest` | body 없음 | 아침 작업 추가 |
| PUT | `/morning-tasks/{dailyPlanId}/{taskId}` | path + `MindSweepRequest` | body 없음 | 아침 작업 수정 |
| DELETE | `/morning-tasks/{dailyPlanId}/{taskId}` | path | body 없음 | 아침 작업 삭제 |
| PATCH | `/morning-tasks/{dailyPlanId}/{taskId}/complete` | path | body 없음 | 완료 처리 |
| PATCH | `/morning-tasks/{dailyPlanId}/{taskId}/uncomplete` | path | body 없음 | 완료 취소 |
| GET | `/daily-plans/{dailyPlanId}/top-picks` | path | `TopPicksResponse[]` | Top Pick 목록 |
| POST | `/daily-plans/{dailyPlanId}/top-picks/{taskId}` | path + `TopPicksRequest` | body 없음 | 작업을 Top Pick으로 선택 |
| DELETE | `/daily-plans/{dailyPlanId}/top-picks/{taskId}` | path | body 없음 | Top Pick 선택 해제 |

### 3.3 Timetable

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| GET | `/timetables?dailyPlanId={uuid}` | query `dailyPlanId` | `TimetableResponse` | 일일 계획의 시간표 조회 |
| POST | `/timetables/{timetableId}/slots/tasks/{taskId}/top-picks` | path + `AssignTopPickSlotRequest` | body 없음 | Top Pick 작업 슬롯 배정 |
| POST | `/timetables/{timetableId}/slots/tasks/{taskId}` | path + `AssignTaskSlotRequest` | body 없음 | 일반 작업 슬롯 배정 |
| POST | `/timetables/{timetableId}/slots/daily-plans/{dailyPlanId}/direct` | path + `AddDirectSlotRequest` | body 없음 | 작업 없이 직접 슬롯 추가 |
| PATCH | `/timetables/{timetableId}/slots/{slotId}/reschedule` | path + `RescheduleSlotRequest` | body 없음 | 슬롯 시간 변경 |
| DELETE | `/timetables/{timetableId}/slots/{slotId}` | path | body 없음 | 슬롯 제거 |

시간 범위는 시작이 종료보다 앞서야 하고 기존 슬롯과 겹칠 수 없다. 직접 슬롯의 content는 연결 Task가 없을 때 표시되는 스냅샷이다.

### 3.4 Exam Schedule

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/exam-schedules` | `ExamScheduleRequest` | `ExamScheduleResponse` | 시험 일정 생성 |
| GET | `/exam-schedules` | 없음 | `ExamScheduleResponse[]` | 내 시험 일정 전체 조회 |
| GET | `/exam-schedules/next` | 없음 | `ExamScheduleResponse` | 가장 가까운 시험 조회 |
| GET | `/exam-schedules/season-mode` | 없음 | `SeasonModeResponse` | 현재 시험 시즌 모드 조회 |
| GET | `/exam-schedules/{examScheduleId}` | path | `ExamScheduleResponse` | 시험 일정 단건 조회 |
| PATCH | `/exam-schedules/{examScheduleId}` | path + `ExamScheduleRequest` | `ExamScheduleResponse` | 시험 일정 수정 |
| DELETE | `/exam-schedules/{examScheduleId}` | path | body 없음 | 시험 일정 삭제 |

### 3.5 Focus Session

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/focus-sessions/start` | `StartFocusSessionRequest` | `FocusSessionResponse` | 집중 세션 시작 |
| PATCH | `/focus-sessions/stop` | 없음 | `FocusSessionResponse` | 진행 중 세션 종료 |
| GET | `/focus-sessions/today` | 없음 | `TodayFocusSessionsResponse` | 오늘 집중 세션과 합계 조회 |
| GET | `/focus-sessions/recovery-card` | 없음 | `RecoveryCardResponse` | 현재 복구 가이드 조회 |
| POST | `/focus-sessions/recovery-card/actions` | `RecordRecoveryCardActionRequest` | `204`, body 없음 | 복구 카드 행동 기록 |

`presetMinutes=null`이면 무제한 타이머다. 값이 있으면 도메인이 허용하는 `3`, `5`, `10`, `25` 중 하나여야 한다.

### 3.6 Focus Statistics

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| GET | `/focus-statistics/daily?targetDate={date}` | query `targetDate` | `FocusPeriodStatisticsResponse` | 일간 통계 |
| GET | `/focus-statistics/weekly?targetDate={date}` | query `targetDate` | `FocusPeriodStatisticsResponse` | targetDate 포함 주간 통계 |
| GET | `/focus-statistics/monthly?targetDate={date}` | query `targetDate` | `FocusPeriodStatisticsResponse` | targetDate 포함 월간 통계 |
| GET | `/focus-statistics/time-of-day?targetDate={date}` | query `targetDate` | `FocusTimeOfDayStatisticsResponse` | 시간대별 집중 통계 |
| GET | `/focus-statistics/timing-recommendation` | 없음 | `FocusTimingRecommendationResponse` | 집중 추천 시간대 |

통계 상태는 `FINAL`, `PARTIAL`, `FUTURE_EMPTY`, 데이터 출처는 `NONE`, `SUMMARY`, `RAW`, `MIXED`다.

### 3.7 Feedback

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/tiny-wins` | `TinyWinRequest` | body 없음 | 작은 성과 생성 |
| GET | `/tiny-wins` | 없음 | `TinyWinResponse[]` | 내 작은 성과 전체 조회 |
| GET | `/tiny-wins/{tinyWinId}` | path | `TinyWinResponse` | 작은 성과 단건 조회 |
| PATCH | `/tiny-wins/{tinyWinId}/title` | path + `UpdateTitleTinyWinRequest` | body 없음 | 제목 수정 |
| PATCH | `/tiny-wins/{tinyWinId}/content` | path + `UpdateContentTinyWinRequest` | body 없음 | 본문 수정 |
| DELETE | `/tiny-wins/{tinyWinId}` | path | body 없음 | 삭제 |
| POST | `/daily-reflections` | `CreateDailyReflectionRequest` | body 없음 | 일일 회고 생성 |
| GET | `/daily-reflections?targetDate={date}` | query `targetDate` | `DailyReflectionResponse` | 날짜별 회고 조회 |
| PATCH | `/daily-reflections/{dailyReflectionId}` | path + `UpdateDailyReflectionRequest` | body 없음 | 회고 수정 |

### 3.8 Future Vision

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/future-vision` | multipart `CreateFutureVisionRequest` | body 없음 | 미래 비전 생성 |
| GET | `/future-vision` | 없음 | `FutureVisionResponse` | 전체 미래 비전 조회 |
| GET | `/future-vision/weekly` | 없음 | `WeeklyVisionResponse` | 주간 비전 조회 |
| GET | `/future-vision/yearly` | 없음 | `YearlyVisionResponse` | 연간 비전 조회 |
| PATCH | `/future-vision/weekly` | multipart `UpdateWeeklyVisionRequest` | body 없음 | 주간 이미지 교체 |
| PATCH | `/future-vision/yearly` | multipart `UpdateYearlyVisionRequest` | body 없음 | 연간 이미지와 설명 수정 |

### 3.9 Study Room

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/rooms` | `CreateRoomRequest` | `CreateRoomResponse` | 공개/비공개 방 생성 |
| GET | `/rooms` | 없음 | `PublicRoomResponse[]` | 공개 방 목록 |
| GET | `/rooms/{roomId}` | path | `RoomDetailResponse` | 방 상세와 참여자 목록 |
| POST | `/rooms/join` | `JoinRoomRequest` | body 없음 | roomId와 초대 코드로 입장 |
| GET | `/rooms/{roomId}/invite-code` | path | `RoomInviteCodeResponse` | 방장 초대 코드 조회 |
| POST | `/rooms/{roomId}/invite-code/reissue` | path | `RoomInviteCodeResponse` | 방장 초대 코드 재발급 |
| POST | `/rooms/{roomId}/leave` | path | body 없음 | 방 나가기; 방장은 필요 시 위임/해산 |
| DELETE | `/rooms/{roomId}/participants/{targetUserId}` | path | body 없음 | 방장이 참여자 강퇴 |
| GET | `/rooms/{roomId}/participants` | path | `ParticipantResponse[]` | 방 참여자 목록 |
| PATCH | `/rooms/{roomId}/participants/focus` | path | body 없음 | 내 방 세션을 집중으로 전환 |
| PATCH | `/rooms/{roomId}/participants/break` | path | body 없음 | 내 방 세션을 휴식으로 전환 |
| GET | `/my-participations` | 없음 | `MyParticipationResponse[]` | 현재 사용자의 방 참여 목록 |

방 Visibility는 목록 공개 여부만 결정한다. 공개 방도 입장에는 inviteCode가 필요하다.

### 3.10 Accountability

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/accountability-relations` | `VisibilityPolicyRequest` | body 없음 | 내가 subject인 감시 관계 생성 |
| POST | `/accountability-relations/join` | `JoinAccountabilityRelationRequest` | `FriendAccountabilityRelationResponse` | 초대 코드로 watcher 참여 후 허용 대상 반환 |
| POST | `/accountability-relations/invite-code/reissue` | 없음 | `InviteCodeResponse` | 초대 코드 재발급 |
| GET | `/accountability-relations/invite-code/status` | 없음 | `InviteCodeStatusResponse` | 초대 코드 상태 조회 |
| GET | `/accountability-relations/friends` | 없음 | `FriendAccountabilityStatusResponse` | 나를 보는 친구/내가 보는 친구 조회 |
| PATCH | `/accountability-relations/visibility-policy` | `VisibilityPolicyRequest` | `FriendAccountabilityRelationResponse` | 공개할 감시 대상 변경 |
| DELETE | `/accountability-relations/watcher` | 없음 | body 없음 | 내 관계의 watcher 연결 해제 |
| DELETE | `/accountability-relations/watching` | 없음 | body 없음 | 친구 감시 중단 |
| GET | `/accountability-relations/watcher/overview?date={date}` | query `date` | `WatcherOverviewResponse` | 허용 대상과 허용된 날짜별 감시 내용을 한 번에 조회 |
| GET | `/accountability-relations/watcher/focus-sessions?date={date}` | query `date` | `DailyFocusSummaryView` 또는 `204` | 허용된 친구의 날짜별 집중 요약 |
| GET | `/accountability-relations/watcher/focus-sessions/range?from={date}&to={date}` | query `from`, `to` | `DailyFocusSummaryView[]` | 기간 집중 요약 |
| GET | `/accountability-relations/watcher/top-picks?date={date}` | query `date` | `DailyTopPicksSummaryView` 또는 `204` | 날짜별 Top Pick 요약 |
| GET | `/accountability-relations/watcher/top-picks/range?from={date}&to={date}` | query `from`, `to` | `DailyTopPicksSummaryView[]` | 기간 Top Pick 요약 |
| GET | `/accountability-relations/watcher/timetable-tasks?date={date}` | query `date` | `DailyTimetableSummaryView` 또는 `204` | 날짜별 시간표 작업 요약 |
| GET | `/accountability-relations/watcher/timetable-tasks/range?from={date}&to={date}` | query `from`, `to` | `DailyTimetableSummaryView[]` | 기간 시간표 요약 |

Watcher 응답의 `userId`는 문자열이 아니라 `{ "id": "uuid" }` 형태다. 단일 조회와 overview는 마감 스냅샷을 우선 조회하고, 스냅샷이 없으면 현재 원본 데이터에서 live view를 구성한다. 단일 조회는 그래도 데이터가 없으면 204, 기간 조회는 스냅샷 범위 조회로 빈 배열을 포함해 항상 200이다. 상세 예시는 [accountability-watcher-api.md](accountability-watcher-api.md)를 참고한다.

### 3.11 Behavior Profile / Insight

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| POST | `/behavior-profiles` | `CreateBehaviorProfileRequest` | body 없음 | 행동 프로필 생성 |
| GET | `/behavior-profiles/me` | 없음 | `BehaviorProfileResponse` | 내 행동 프로필 조회 |
| PUT | `/behavior-profiles/me` | `UpdateBehaviorProfileRequest` | body 없음 | 행동 프로필 전체 수정 |
| GET | `/behavior-profiles/adjustments` | 없음 | `ProfileAdjustmentResponse[]` | 대기 중인 조정 제안 조회 |
| POST | `/behavior-profiles/adjustments/{suggestionId}/accept` | path | body 없음 | 제안 수락 및 프로필 반영 |
| POST | `/behavior-profiles/adjustments/{suggestionId}/dismiss` | path | body 없음 | 제안 거절 |
| GET | `/insights/latest` | 없음 | `InsightReportResponse` | 최신 행동 인사이트 조회 |
| GET | `/insights/{insightReportId}` | path | `InsightReportResponse` | 인사이트 단건 조회 |
| POST | `/insights/generate` | 선택 JSON `GenerateInsightReportRequest` | `InsightReportResponse` | `accountId=admin` 전용 수동 생성; 실제 OpenAI 호출 포함 |

인사이트 생성 body가 없거나 두 날짜 중 하나라도 없으면 최근 30일을 사용한다. 일반 사용자는 403이다.

### 3.12 Notification / Analytics / Home

| Method | Path | 요청 | 응답 | 설명 |
|---|---|---|---|---|
| GET | `/notification/preferences` | 없음 | `NotificationPreferenceResponse` | 내 알림 설정 조회 |
| PATCH | `/notification/preferences` | `NotificationPreferenceRequest` | `NotificationPreferenceResponse` | 알림 설정 수정 |
| POST | `/analytics/events` | `AnalyticsEventRequest` | `AnalyticsEventResponse` | 사용자 분석 이벤트 기록 |
| GET | `/analytics/events?from={date}&to={date}&eventType={type}` | query `from`, `to`, 선택 `eventType` | `AnalyticsEventResponse[]` | 기간/유형별 내 이벤트 조회 |
| GET | `/home/today` | 없음 | `HomeTodayResponse` | 오늘의 비전·Top Pick·시간표·시험·알림·친구 상태 통합 조회 |

## 4. 요청 스키마

JSON property 이름은 아래 필드명과 동일하다. `required`가 아니면 nullable 또는 선택 값이다.

| 스키마 | 필드와 제약 |
|---|---|
| `LocalSignupRequest` | `email: string` required/email/max 255, `accountId: string` required/max 20, `profileName: string` required/max 20, `profileImage: file` required, `password: string` required/8..20 |
| `LocalLoginRequest` | `accountId: string` required/max 20, `password: string` required/8..20 |
| `OauthCompleteRequest` | `code: string` required |
| `OauthProfileSetupRequest` | `accountId: string` required/max 20, `profileName: string` required/max 20, `profileImage: file` required, `password: string` required/8..20 |
| `TokenReissueRequest` | `refreshToken: string` required |
| `DailyPlanRequest` | `planDate: LocalDate` required |
| `MindSweepRequest` | `content: string` required/max 255 |
| `TopPicksRequest` | `estimatedMinutes: int` positive, `memo: string` required/max 255 |
| `AssignTopPickSlotRequest` | `startTime: LocalTime` required, `endTime: LocalTime` required |
| `AssignTaskSlotRequest` | `startTime: LocalTime` required, `endTime: LocalTime` required |
| `AddDirectSlotRequest` | `content: string` required/max 255, `startTime: LocalTime` required, `endTime: LocalTime` required |
| `RescheduleSlotRequest` | `startTime: LocalTime` required, `endTime: LocalTime` required |
| `ExamScheduleRequest` | `examType: ExamType` required, `title: string` required/max 100, `examDate: LocalDate` required, `subject: string` max 50 |
| `StartFocusSessionRequest` | `presetMinutes: integer` nullable; null 또는 3/5/10/25 |
| `RecordRecoveryCardActionRequest` | `action: RecoveryCardAction` required |
| `TinyWinRequest` | `title: string` required/max 30, `content: string` required/max 3000 |
| `UpdateTitleTinyWinRequest` | `title: string` required/max 30 |
| `UpdateContentTinyWinRequest` | `content: string` required/max 3000. 실제 Java 클래스명은 `UpdateContentTInyWinRequest`로 대소문자 오타가 있으나 JSON에는 영향 없음 |
| `CreateDailyReflectionRequest` | `reflectionDate: LocalDate` required, `whatWentWell: string` required/max 500, `whatBrokeDown: string` required/max 1000, `ifCondition: string` required/max 500, `thenAction: string` required/max 500 |
| `UpdateDailyReflectionRequest` | 생성 요청에서 `reflectionDate`를 제외한 네 문자열 필드와 동일 |
| `CreateFutureVisionRequest` | `weeklyVisionImageUrl: file` required, `yearlyVisionImageUrl: file` required, `yearlyVisionDescription: string` required/max 100 |
| `UpdateWeeklyVisionRequest` | `weeklyVisionImageUrl: file` required |
| `UpdateYearlyVisionRequest` | `yearlyVisionImageUrl: file` required, `yearlyVisionDescription: string` required/max 100 |
| `CreateRoomRequest` | `name: string` required/max 20, `visibility: Visibility` required |
| `JoinRoomRequest` | `roomId: UUID`, `inviteCode: string`; DTO Bean Validation은 없고 서비스/도메인에서 검증 |
| `VisibilityPolicyRequest` | `targets: Set<MonitoringTarget>` required/non-empty |
| `JoinAccountabilityRelationRequest` | `inviteCode: string` required/max 10 |
| `CreateBehaviorProfileRequest` | `executionDifficulty`, `socialPreference`, `recoveryStyle`, `examTrack`, `coachingMode` required enum; `preferredFocusStartHour`, `preferredFocusEndHour` required integer 0..23 |
| `UpdateBehaviorProfileRequest` | 생성 요청과 동일 |
| `GenerateInsightReportRequest` | `periodStart: LocalDate`, `periodEnd: LocalDate`; body 자체도 선택 |
| `NotificationPreferenceRequest` | `dailyFocusEnabled`, `dailyTopPicksEnabled`, `dailyTimetableEnabled`, `accountabilityEnabled`, `schoolHoursQuietEnabled`, `weekendSchoolQuietEnabled`: required boolean; `schoolHoursStart`, `schoolHoursEnd`: required `HH:mm`; `maxDailyPushCount`: required integer 0..10 |
| `AnalyticsEventRequest` | `eventType: AnalyticsEventType` required, `properties: Map<string,string>` nullable/max 20 entries |
| `ChatMessageRequest` | `content: string` required/max 500 |

### 4.1 Enum 값

| Enum | 값 |
|---|---|
| `Visibility` | `PUBLIC`, `PRIVATE` |
| `TaskType` | `GENERAL`, `MORNING` |
| `ExamType` | `NAESIN`, `MOPYUNG`, `HAKPYUNG`, `SUNUNG`, `OTHER` |
| `SeasonMode` | `SUNUNG_INTENSIVE`, `NAESIN_INTENSIVE`, `MOPYUNG_FOCUSED`, `BASELINE_MODE` |
| `RecoveryCardAction` | `START`, `REFLECT`, `DISMISS` |
| `RecoveryType` | `POST_EXAM_RECOVERY`, `LONG_ABSENCE`, `MISSED_FOCUS`, `INCOMPLETE_TOP_PICK`, `BOTH`, `NONE` |
| `SessionMode` | `WAITING`, `FOCUS`, `REST`, `ENDED` |
| `MonitoringTarget` | `FOCUS_SESSION`, `TOP_PICKS`, `TIMETABLE_TASK` |
| `ExecutionDifficulty` | `LOW`, `MEDIUM`, `HIGH` |
| `SocialPreference` | `LOW`, `MEDIUM`, `HIGH` |
| `RecoveryStyle` | `QUICK_RESTART`, `NEEDS_REFLECTION`, `SLOW_REBUILDER` |
| `ExamTrack` | `UNDECIDED`, `NAESIN`, `MOPYUNG_SUNUNG`, `BOTH` |
| `CoachingMode` | `GENTLE`, `NEUTRAL`, `STRICT` |
| `ProfileAdjustmentTarget` | `FOCUS_HOURS`, `EXECUTION_DIFFICULTY`, `RECOVERY_STYLE` |
| `AdjustmentSuggestionStatus` | `PENDING`, `ACCEPTED`, `DISMISSED` |
| `InsightReportStatus` | `METRICS_READY`, `COMPLETED`, `NARRATIVE_FAILED` |
| `AnalyticsEventType` | `SIGNUP`, `ONBOARDING_STARTED`, `ONBOARDING_SKIPPED`, `BEHAVIOR_PROFILE_CREATED`, `FUTURE_VISION_CREATED`, `MORNING_TASK_CREATED`, `TOP_PICK_SELECTED`, `TIMETABLE_SLOT_CREATED`, `FOCUS_SESSION_STARTED`, `FOCUS_SESSION_COMPLETED`, `FOCUS_SESSION_ABANDONED`, `FOCUS_SESSION_AUTO_CLOSED`, `TINY_WIN_CREATED`, `DAILY_REFLECTION_CREATED`, `RECOVERY_CARD_VIEWED`, `RECOVERY_CARD_ACTIONED`, `ACCOUNTABILITY_INVITE_SENT`, `ACCOUNTABILITY_FRIEND_JOINED`, `SCHOOL_HOURS_MUTE_TOGGLED`, `EXAM_REGISTERED` |

## 5. 응답 스키마

표기법: `T[]`는 배열, `T?`는 null 가능, `Map<K,V>`는 JSON object다.

### 5.1 Account / Planning

| 스키마 | 필드 |
|---|---|
| `OnboardingContextResponse` | `pendingSchoolHours: boolean` |
| `TokenResponse` | `accessToken: string`, `refreshToken: string` |
| `OauthCompleteResponse` | `accessToken: string?`, `refreshToken: string?`, `pendingToken: string?`, `isProfileCompleted: boolean` |
| `ProfileSetupResponse` | `accessToken: string`, `refreshToken: string`, `isProfileCompleted: boolean` |
| `ProfileResponse` | `userId: UUID`, `accountId: string`, `profileName: string`, `profileImage: string`, `credentials: CredentialResponse[]` |
| `CredentialResponse` | `email: string`, `provider: LOCAL\|GOOGLE\|NAVER` |
| `DailyPlanResponse` | `dailyPlanId: UUID`, `planDate: LocalDate`, `tasks: TaskResponse[]`, `morningTasks: TaskResponse[]` |
| `TaskResponse` | `taskId: UUID`, `content: string`, `completed: boolean`, `taskType: TaskType`, `topPicked: boolean`, `topPickDetail: TopPickDetailResponse?` |
| `TopPickDetailResponse` | `estimatedMinutes: int`, `memo: string` |
| `MindSweepResponse` | `taskId: UUID`, `content: string`, `completed: boolean` |
| `TopPicksResponse` | `taskId: UUID`, `content: string`, `completed: boolean`, `estimatedMinutes: int`, `memo: string` |
| `TimetableResponse` | `timetableId: UUID`, `dailyPlanId: UUID`, `topPickTotal: int`, `slots: SlotResponse[]` |
| `SlotResponse` | `slotId: UUID`, `taskId: UUID?`, `content: string`, `startTime: LocalTime`, `endTime: LocalTime`, `topPick: boolean` |
| `ExamScheduleResponse` | `examScheduleId: UUID`, `examType: ExamType`, `title: string`, `examDate: LocalDate`, `subject: string?`, `daysUntil: long`, `seasonMode: SeasonMode` |
| `SeasonModeResponse` | `seasonMode: SeasonMode`, `nextExamSchedule: ExamScheduleResponse?` |

### 5.2 Focus / Feedback / Vision

| 스키마 | 필드 |
|---|---|
| `FocusSessionResponse` | `focusSessionId: UUID`, `startedAt: Instant`, `endedAt: Instant?`, `recordedElapsedSeconds: long?`, `elapsedSeconds: long`, `inProgress: boolean`, `unlimited: boolean`, `presetMinutes: int?`, `presetSeconds: int?`, `presetCompletionRate: double?` |
| `TodayFocusSessionsResponse` | `targetDate: LocalDate`, `queriedAt: Instant`, `totalFocusSeconds: long`, `focusing: boolean`, `sessions: FocusSessionResponse[]` |
| `RecoveryCardResponse` | `needsRecovery: boolean`, `recoveryType: RecoveryType`, `suggestedAction: string`, `suggestedDurationMinutes: int?`, `yesterdayFocusSeconds: long`, `yesterdayTopPickCompletionRate: double`, `postExamMode: boolean`, `recentExamScheduleId: UUID?`, `recentExamType: ExamType?`, `recentExamTitle: string?`, `recentExamDate: LocalDate?`, `recentExamSubject: string?`, `daysSinceRecentExam: long?`, `daysSinceLastSession: long?` |
| `FocusPeriodStatisticsResponse` | `targetDate: LocalDate`, `queriedAt: Instant`, `periodStartDate: LocalDate`, `periodEndDate: LocalDate`, `dayCount: int`, `coveredDayCount: int`, `totalFocusSeconds: long`, `averageDailyFocusSeconds: long`, `status`, `dataSource` |
| `FocusTimeOfDayStatisticsResponse` | `targetDate: LocalDate`, `queriedAt: Instant`, `totalFocusSeconds: long`, `status`, `dataSource`, `hourlyBuckets: FocusTimeBucketResponse[]` |
| `FocusTimeBucketResponse` | `hourOfDay: int`, `focusSeconds: long` |
| `FocusTimingRecommendationResponse` | `targetDate: LocalDate`, `queriedAt: Instant`, `recommendedHours: RecommendedHour[]`, `reason: string`, `basedOnData: boolean` |
| `RecommendedHour` | `hourOfDay: int`, `averageFocusSeconds: long` |
| `TinyWinResponse` | `tinyWinId: UUID`, `title: string`, `content: string`, `localDate: LocalDate` |
| `DailyReflectionResponse` | `dailyReflectionId: UUID`, `reflectionDate: LocalDate`, `whatWentWell`, `whatBrokeDown`, `ifCondition`, `thenAction`: string |
| `FutureVisionResponse` | `futureVisionId: UUID`, `weeklyVisionImageUrl: string`, `yearlyVisionImageUrl: string`, `yearlyVisionDescription: string`, `yearlyVisionCreatedAt: LocalDate` |
| `WeeklyVisionResponse` | `futureVisionId: UUID`, `weeklyVisionImageUrl: string` |
| `YearlyVisionResponse` | `futureVisionId: UUID`, `yearlyVisionImageUrl: string`, `yearlyVisionDescription: string`, `yearlyVisionCreatedAt: LocalDate` |

### 5.3 Study Room / Accountability

| 스키마 | 필드 |
|---|---|
| `CreateRoomResponse` | `roomId: UUID`, `inviteCode: string` |
| `PublicRoomResponse` | `roomId: UUID`, `name: string`, `createdAt: LocalDateTime` |
| `RoomInviteCodeResponse` | `inviteCode: string` |
| `RoomDetailResponse` | `roomId: UUID`, `name: string`, `visibility: Visibility`, `leaderUserId: UUID`, `currentCount: int`, `createdAt: LocalDateTime`, `participants: ParticipantResponse[]` |
| `ParticipantResponse` | `participantId: UUID`, `userId: UUID`, `participantName: string`, `sessionMode: SessionMode`, `joinedAt: LocalDateTime` |
| `MyParticipationResponse` | `roomId: UUID`, `participantId: UUID`, `sessionMode: SessionMode`, `joinedAt: LocalDateTime` |
| `InviteCodeResponse` | `inviteCode: string`, `expiresAt: LocalDateTime` |
| `InviteCodeStatusResponse` | `inviteCode: string?`, `expiredAt: LocalDateTime?`, `expired: boolean`, `reissuable: boolean`, `watcherConnected: boolean` |
| `FriendAccountabilityStatusResponse` | `watchedByFriends: FriendAccountabilityRelationResponse[]`, `watchingFriends: FriendAccountabilityRelationResponse[]` |
| `FriendAccountabilityRelationResponse` | `accountabilityRelationId: UUID`, `subjectUserId: UUID`, `watcherUserId: UUID?`, `watcherConnected: boolean`, `allowedTargets: MonitoringTarget[]` |
| `WatcherOverviewResponse` | `relation: FriendAccountabilityRelationResponse`, `date: LocalDate`, `focusSessions: DailyFocusSummaryView?`, `topPicks: DailyTopPicksSummaryView?`, `timetableTasks: DailyTimetableSummaryView?`; 허용되지 않았거나 데이터가 없으면 해당 필드는 `null` |
| `DailyFocusSummaryView` | `userId: {id: UUID}`, `date: LocalDate`, `totalSeconds: long`, `sessionCount: int`, `items: DailyFocusSummaryItemView[]` |
| `DailyFocusSummaryItemView` | `startedAtSnapshot: Instant`, `endedAtSnapshot: Instant`, `recordedDurationSecondsSnapshot: long?`, `overlapStartedAt: Instant`, `overlapEndedAt: Instant`, `overlapSeconds: long`, `displayOrder: int` |
| `DailyTopPicksSummaryView` | `userId: {id: UUID}`, `date: LocalDate`, `totalCount: int`, `completedCount: int`, `items: DailyTopPicksSummaryItemView[]` |
| `DailyTopPicksSummaryItemView` | `content: string`, `completed: boolean`, `estimatedMinutes: int?`, `memo: string?`, `displayOrder: int` |
| `DailyTimetableSummaryView` | `userId: {id: UUID}`, `date: LocalDate`, `totalCount: int`, `completedCount: int`, `items: DailyTimetableSummaryItemView[]` |
| `DailyTimetableSummaryItemView` | `contentSnapshot: string`, `completedSnapshot: boolean`, `startTimeSnapshot: LocalTime`, `endTimeSnapshot: LocalTime`, `topPickSnapshot: boolean`, `displayOrder: int` |

### 5.4 Personalization / Insight / Other

| 스키마 | 필드 |
|---|---|
| `BehaviorProfileResponse` | `behaviorProfileId: UUID`, `executionDifficulty`, `socialPreference`, `recoveryStyle`, `examTrack`, `preferredFocusStartHour: int`, `preferredFocusEndHour: int`, `coachingMode` |
| `ProfileAdjustmentResponse` | `id: UUID`, `target`, `declaredValue: string`, `observedValue: string`, `suggestedStartHour: int?`, `suggestedEndHour: int?`, `suggestedValue: string?`, `message: string`, `status`, `createdAt: Instant` |
| `InsightReportResponse` | `insightReportId: UUID`, `periodStart: LocalDate`, `periodEnd: LocalDate`, `status: InsightReportStatus`, `generatedAt: Instant`, `metrics: InsightMetrics`, `narrative: InsightNarrativeResponse?` |
| `InsightMetrics` | `totalFocusSeconds: long`, `completedSessionCount: int`, `abandonedSessionCount: int`, `focusCompletionRate: double`, `peakFocusHour: int?`, `activeDayCount: int`, `topPickSelectedCount: int`, `reflectionCount: int`, `tinyWinCount: int`, `quickResumeRate: double?` |
| `InsightNarrativeResponse` | `summary: string`, `strengths: string[]`, `improvements: string[]`, `patterns: string[]`, `motivation: string`, `profileSuggestion: string` |
| `NotificationPreferenceResponse` | request 필드 전부 + `notificationPreferenceId: UUID`, `sleepHoursQuietEnabled: boolean`; 시간은 `HH:mm` |
| `AnalyticsEventResponse` | `analyticsEventId: UUID`, `eventType: AnalyticsEventType`, `occurredAt: Instant`, `properties: Map<string,string>` |
| `HomeTodayResponse` | `targetDate: LocalDate`, `futureVision: FutureVisionResponse?`, `topPicks: TopPicksResponse[]`, `timetable: TimetableResponse?`, `seasonMode: SeasonMode`, `nextExamSchedule: ExamScheduleResponse?`, `notificationPreference: NotificationPreferenceResponse`, `friendAccountability: HomeFriendAccountabilityStatusResponse`, `showFocusTimingCard: boolean` |
| `HomeFriendAccountabilityStatusResponse` | `relationCreated: boolean`, `watchedByFriend: boolean`, `watchingFriend: boolean`, `inviteCodeStatus: InviteCodeStatusResponse?` |

## 6. STOMP WebSocket 채팅

### 6.1 연결과 인증

- SockJS endpoint: `/ws`
- STOMP application prefix: `/app`
- broker prefixes: `/topic`, `/queue`
- user prefix: `/user`
- `CONNECT` native header에 REST와 같은 JWT 헤더를 보낸다.
- 송신과 구독 모두 해당 room의 현재 Participant여야 한다.

### 6.2 메시지 계약

| 동작 | Destination | Payload |
|---|---|---|
| 채팅 송신 | `/app/rooms/{roomId}/chat` | `ChatMessageRequest` |
| 채팅 구독 | `/topic/rooms/{roomId}/chat` | `ChatMessagePayload` 수신 |
| 개인 오류 구독 | `/user/queue/errors` | `ChatErrorPayload` 수신 |

`ChatMessagePayload`은 `roomId: UUID`, `senderId: UUID`, `senderName: string`, `content: string`, `sentAt: Instant`다. `ChatErrorPayload`은 `statusCode: int`, `message: string`, `timestamp: Instant`다.

## 7. 오류 카탈로그

내부 `ErrorCode`를 HTTP 상태별로 묶으면 다음과 같다. 이름은 서버 개발자를 위한 추적 키이며 현재 응답 body에는 포함되지 않는다.

| 상태 | 내부 오류 |
|---:|---|
| 400 | `INVALID_REQUEST`, `IMAGE_NOT_FOUND`, `INVALID_FILE_EXTENSION`, `INVALID_FUTURE_VISION`, `INVALID_TASK_TYPE`, `TASK_ALREADY_COMPLETED`, `CORE_SELECTED_LIMIT_EXCEEDED`, `TIME_OVERLAP`, `INVALID_TIME_RANGE`, `INVALID_DATE_RANGE`, `TOP_PICKS_NOT_FULLY_ASSIGNED`, `TOP_PICK_SLOT_LIMIT_EXCEEDED`, `NOT_TOP_PICKED_TASK`, `INVALID_TOP_PICK_ESTIMATED_MINUTES`, `INVALID_TOP_PICK_MEMO`, `INVALID_EXAM_SCHEDULE`, `INVALID_TINY_WIN`, `INVALID_DAILY_REFLECTION`, `LEADER_CANNOT_KICK_SELF`, `INVALID_INVITE_CODE`, `ALREADY_FOCUSING`, `NOT_FOCUSING`, `PARTICIPANT_ALREADY_ENDED`, `FOCUS_SESSION_ALREADY_COMPLETED`, `INVALID_FOCUS_SESSION`, `INVALID_NOTIFICATION_PREFERENCE`, `INVALID_ANALYTICS_EVENT`, `INVITE_CODE_NOT_GENERATED`, `INVITE_CODE_EXPIRED`, `CANNOT_JOIN_OWN_ACCOUNTABILITY_RELATION`, `INVALID_BEHAVIOR_PROFILE`, `INVALID_CHAT_MESSAGE`, `INVALID_INSIGHT_PERIOD` |
| 401 | `EXPIRED_JWT`, `INVALID_JWT`, `LOGIN_FAILED`, `PASSWORD_MISMATCH`, `REFRESH_TOKEN_NOT_FOUND` |
| 403 | `NOT_LEADER`, `MONITORING_TARGET_NOT_ALLOWED`, `NOT_SUBJECT_USER`, `NOT_WATCHER_USER`, `CHAT_NOT_ALLOWED`, `INSIGHT_GENERATION_FORBIDDEN` |
| 404 | `ACCOUNT_NOT_FOUND`, `USER_NOT_FOUND`, `OAUTH_CALLBACK_NOT_FOUND`, `PENDING_OAUTH_NOT_FOUND`, `DAILY_PLAN_NOT_FOUND`, `FUTURE_VISION_NOT_FOUND`, `TASK_NOT_FOUND`, `TIMETABLE_NOT_FOUND`, `SLOT_NOT_FOUND`, `TOP_PICK_DETAIL_NOT_FOUND`, `EXAM_SCHEDULE_NOT_FOUND`, `TINY_WIN_NOT_FOUND`, `DAILY_REFLECTION_NOT_FOUND`, `PARTICIPANT_NOT_FOUND`, `ROOM_NOT_FOUND`, `FOCUS_SESSION_NOT_FOUND`, `DEVICE_TOKEN_NOT_FOUND`, `ACCOUNTABILITY_RELATION_NOT_FOUND`, `BEHAVIOR_PROFILE_NOT_FOUND`, `INSIGHT_REPORT_NOT_FOUND`, `PROFILE_ADJUSTMENT_SUGGESTION_NOT_FOUND` |
| 409 | `DUPLICATE_ACCOUNT_ID`, `DUPLICATE_USER`, `DUPLICATE_EMAIL`, `DAILY_PLAN_ALREADY_EXISTS`, `FUTURE_VISION_ALREADY_EXISTS`, `DAILY_REFLECTION_ALREADY_EXISTS`, `ALREADY_JOINED`, `FOCUS_SESSION_ALREADY_IN_PROGRESS`, `ACCOUNTABILITY_RELATION_ALREADY_EXISTS`, `WATCHER_ALREADY_EXISTS`, `BEHAVIOR_PROFILE_ALREADY_EXISTS` |
| 413 | `FILE_SIZE_EXCEEDED` |
| 500 | `INTERNAL_SERVER_ERROR`, `USER_CREATION_FAILED`, `FILE_UPLOAD_FAILED`, `FILE_DELETE_FAILED`, `FUTURE_VISION_CREATION_FAILED`, `FUTURE_VISION_UPDATE_FAILED`, `INVITE_CODE_GENERATION_FAILED` |

## 8. 구현상 주의점

- 다수의 생성·수정·삭제 API가 REST 관례의 201/204가 아니라 `200`을 반환한다. 클라이언트는 현재 계약을 따르되 향후 상태 코드 표준화 시 변경 가능성을 고려해야 한다.
- `ErrorResponse`에 안정적인 기계 판독용 error code가 없다. 프론트엔드가 한국어 message 문자열에 분기 로직을 두면 메시지 변경에 취약하다.
- `JoinRoomRequest`에는 Bean Validation이 없어 null/blank 입력이 전역 400이 아니라 서비스/도메인 경로에 따라 처리될 수 있다.
- OAuth 성공 redirect에는 실제 JWT 또는 pendingToken이 아니라 5분짜리 1회용 callback code만 포함된다. 프론트엔드는 반드시 `POST /auth/oauth/complete`로 code를 교환한 뒤 기존 사용자는 JWT를 저장하고, 신규 사용자는 응답의 `pendingToken`으로 프로필 설정을 완료해야 한다.
- Watcher summary의 `userId`만 값 객체가 그대로 노출되어 다른 응답의 UUID 문자열 형태와 다르다.
- `POST /insights/generate`의 관리자 판별은 role이 아니라 `accountId == "admin"` 하드코딩이다.
- `NotificationPreferenceResponse`에는 요청으로 직접 설정하지 않는 `sleepHoursQuietEnabled`가 추가로 포함된다.
- API 버전 prefix(`/api/v1`)와 OpenAPI/Swagger 설정은 현재 코드에 없다.

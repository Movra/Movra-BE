# MORVA API 명세서

## 공통 사항

- **Base URL**: `/`
- **인증**: JWT Bearer Token (`Authorization: Bearer {accessToken}`)
- **Content-Type**: `application/json` (파일 업로드 시 `multipart/form-data`)
- **에러 응답 형식**:
```json
{
  "status": 404,
  "message": "데일리 플랜을 찾을 수 없습니다."
}
```

---

## 1. Auth API

> 모든 Auth API는 인증 불필요 (permitAll)

### 1.1 로컬 회원가입

```
POST /auth/signup
Content-Type: multipart/form-data
```

**Request**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | O | 이메일 (최대 255자) |
| accountId | String | O | 계정 ID (최대 30자) |
| profileName | String | O | 프로필 이름 (최대 20자) |
| profileImage | MultipartFile | O | 프로필 이미지 |
| password | String | O | 비밀번호 (8~20자) |

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 409 | DUPLICATE_ACCOUNT_ID | 이미 존재하는 계정 ID |
| 409 | DUPLICATE_EMAIL | 이미 존재하는 이메일 |

---

### 1.2 로컬 로그인

```
POST /auth/login
Content-Type: application/json
```

**Request**
```json
{
  "accountId": "string (최대 30자)",
  "password": "string (8~20자)"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | ACCOUNT_NOT_FOUND | 계정 ID를 찾을 수 없음 |
| 401 | PASSWORD_MISMATCH | 비밀번호 불일치 |

---

### 1.3 OAuth 프로필 설정

```
POST /auth/oauth/profile-setup?pendingToken={pendingToken}
Content-Type: multipart/form-data
```

**Request**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| pendingToken | String (Query) | O | OAuth 대기 토큰 |
| accountId | String | O | 계정 ID (최대 30자) |
| profileName | String | O | 프로필 이름 (최대 20자) |
| profileImage | MultipartFile | O | 프로필 이미지 |
| password | String | O | 비밀번호 (8~20자) |

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "isProfileCompleted": true
}
```

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | PENDING_OAUTH_NOT_FOUND | 대기 중인 OAuth 사용자를 찾을 수 없음 |

---

### 1.4 토큰 재발급

```
POST /auth/reissue
Content-Type: application/json
```

**Request**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 401 | REFRESH_TOKEN_NOT_FOUND | 리프레시 토큰을 찾을 수 없음 |
| 401 | INVALID_JWT | 유효하지 않은 JWT 토큰 |

---

## 2. Daily Plan API

> 인증 필수 (Authorization: Bearer {accessToken})

### 2.1 일일 계획 생성

```
POST /daily-plans
Content-Type: application/json
```

**Request**
```json
{
  "planDate": "2026-03-21"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 409 | DAILY_PLAN_ALREADY_EXISTS | 해당 날짜의 데일리 플랜이 이미 존재 |

---

### 2.2 일일 계획 조회

```
GET /daily-plans?planDate=2026-03-21
```

**Response** `200 OK`
```json
{
  "dailyPlanId": "uuid",
  "planDate": "2026-03-21",
  "tasks": [
    {
      "taskId": "uuid",
      "content": "할 일 내용",
      "completed": false,
      "taskType": "GENERAL",
      "coreSelected": false,
      "coreDetail": null
    }
  ],
  "morningTasks": [
    {
      "taskId": "uuid",
      "content": "아침 루틴",
      "completed": false,
      "taskType": "MORNING",
      "coreSelected": false,
      "coreDetail": null
    }
  ]
}
```

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | DAILY_PLAN_NOT_FOUND | 데일리 플랜을 찾을 수 없음 |

---

## 3. MindSweep API (마인드 스윕)

> 인증 필수

### 3.1 마인드 스윕 전체 조회

```
GET /daily-plans/{dailyPlanId}/mind-sweeps
```

**Response** `200 OK`
```json
[
  {
    "taskId": "uuid",
    "content": "할 일 내용",
    "completed": false
  }
]
```

---

### 3.2 마인드 스윕 추가

```
POST /daily-plans/{dailyPlanId}/mind-sweeps
Content-Type: application/json
```

**Request**
```json
{
  "content": "할 일 내용 (최대 255자)"
}
```

**Response**: `200 OK` (body 없음)

---

### 3.3 마인드 스윕 수정

```
PUT /daily-plans/{dailyPlanId}/mind-sweeps/{taskId}
Content-Type: application/json
```

**Request**
```json
{
  "content": "수정된 내용 (최대 255자)"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | TASK_NOT_FOUND | 태스크를 찾을 수 없음 |
| 400 | TASK_ALREADY_COMPLETED | 완료된 태스크는 수정 불가 |
| 400 | INVALID_TASK_TYPE | 해당 작업 유형이 맞지 않음 |

---

### 3.4 마인드 스윕 삭제

```
DELETE /daily-plans/{dailyPlanId}/mind-sweeps/{taskId}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | TASK_NOT_FOUND | 태스크를 찾을 수 없음 |
| 400 | TASK_ALREADY_COMPLETED | 완료된 태스크는 삭제 불가 |

---

### 3.5 마인드 스윕 완료

```
PATCH /daily-plans/{dailyPlanId}/mind-sweeps/{taskId}/complete
```

**Response**: `200 OK` (body 없음)

---

### 3.6 마인드 스윕 미완료

```
PATCH /daily-plans/{dailyPlanId}/mind-sweeps/{taskId}/uncomplete
```

**Response**: `200 OK` (body 없음)

---

## 4. Morning Task API (아침 태스크)

> 인증 필수

### 4.1 아침 태스크 조회

```
GET /morning-tasks?targetDate=2026-03-21
```

**Response** `200 OK`
```json
[
  {
    "taskId": "uuid",
    "content": "아침 루틴 내용",
    "completed": false
  }
]
```

---

### 4.2 아침 태스크 추가

```
POST /morning-tasks?targetDate=2026-03-21
Content-Type: application/json
```

**Request**
```json
{
  "content": "아침 루틴 내용 (최대 255자)"
}
```

**Response**: `200 OK` (body 없음)

> DailyPlan이 없으면 자동 생성됩니다.

---

### 4.3 아침 태스크 수정

```
PUT /morning-tasks/{dailyPlanId}/{taskId}
Content-Type: application/json
```

**Request**
```json
{
  "content": "수정된 내용 (최대 255자)"
}
```

**Response**: `200 OK` (body 없음)

---

### 4.4 아침 태스크 삭제

```
DELETE /morning-tasks/{dailyPlanId}/{taskId}
```

**Response**: `200 OK` (body 없음)

---

### 4.5 아침 태스크 완료

```
PATCH /morning-tasks/{dailyPlanId}/{taskId}/complete
```

**Response**: `200 OK` (body 없음)

---

### 4.6 아침 태스크 미완료

```
PATCH /morning-tasks/{dailyPlanId}/{taskId}/uncomplete
```

**Response**: `200 OK` (body 없음)

---

## 5. Top Pick API (핵심 태스크)

> 인증 필수

### 5.1 Top Pick 전체 조회

```
GET /daily-plans/{dailyPlanId}/top-picks
```

**Response** `200 OK`
```json
[
  {
    "taskId": "uuid",
    "content": "핵심 태스크 내용",
    "completed": false,
    "estimatedMinutes": 30,
    "memo": "메모 내용"
  }
]
```

---

### 5.2 Top Pick 선택

```
POST /daily-plans/{dailyPlanId}/top-picks/{taskId}
Content-Type: application/json
```

**Request**
```json
{
  "estimatedMinutes": 30,
  "memo": "메모 내용 (최대 255자)"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | TASK_NOT_FOUND | 태스크를 찾을 수 없음 |
| 400 | CORE_SELECTED_LIMIT_EXCEEDED | Top Pick 선택 개수 초과 (최대 3개) |

---

### 5.3 Top Pick 해제

```
DELETE /daily-plans/{dailyPlanId}/top-picks/{taskId}
```

**Response**: `200 OK` (body 없음)

---

## 6. Timetable API (시간표)

> 인증 필수

### 6.1 타임테이블 조회

```
GET /timetables?dailyPlanId={dailyPlanId}
```

**Response** `200 OK`
```json
{
  "timetableId": "uuid",
  "dailyPlanId": "uuid",
  "topPickTotal": 3,
  "slots": [
    {
      "slotId": "uuid",
      "taskId": "uuid",
      "content": "태스크 내용",
      "startTime": "09:00",
      "endTime": "10:00",
      "topPick": true
    }
  ]
}
```

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | TIMETABLE_NOT_FOUND | 타임테이블을 찾을 수 없음 |

---

### 6.2 Top Pick 슬롯 할당

```
POST /timetables/{timetableId}/slots/tasks/{taskId}/top-picks
Content-Type: application/json
```

**Request**
```json
{
  "startTime": "09:00",
  "endTime": "10:00"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 400 | TIME_OVERLAP | 다른 슬롯과 시간이 겹침 |
| 400 | INVALID_TIME_RANGE | 유효하지 않은 시간 범위 |
| 400 | TOP_PICK_SLOT_LIMIT_EXCEEDED | Top Pick 슬롯 개수 초과 |

---

### 6.3 일반 태스크 슬롯 할당

```
POST /timetables/{timetableId}/slots/tasks/{taskId}
Content-Type: application/json
```

**Request**
```json
{
  "startTime": "10:00",
  "endTime": "11:00"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 400 | TOP_PICKS_NOT_FULLY_ASSIGNED | 모든 Top Pick이 먼저 할당되어야 함 |
| 400 | TIME_OVERLAP | 다른 슬롯과 시간이 겹침 |
| 400 | INVALID_TIME_RANGE | 유효하지 않은 시간 범위 |

---

### 6.4 직접 슬롯 추가

```
POST /timetables/{timetableId}/slots/daily-plans/{dailyPlanId}/direct
Content-Type: application/json
```

**Request**
```json
{
  "content": "슬롯 내용 (최대 255자)",
  "startTime": "12:00",
  "endTime": "13:00"
}
```

**Response**: `200 OK` (body 없음)

---

### 6.5 슬롯 시간 변경

```
PATCH /timetables/{timetableId}/slots/{slotId}/reschedule
Content-Type: application/json
```

**Request**
```json
{
  "startTime": "14:00",
  "endTime": "15:00"
}
```

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | SLOT_NOT_FOUND | 슬롯을 찾을 수 없음 |
| 400 | TIME_OVERLAP | 다른 슬롯과 시간이 겹침 |
| 400 | INVALID_TIME_RANGE | 유효하지 않은 시간 범위 |

---

### 6.6 슬롯 제거

```
DELETE /timetables/{timetableId}/slots/{slotId}
```

**Response**: `200 OK` (body 없음)

---

## 7. Future Vision API (미래 비전)

> 인증 필수

### 7.1 미래 비전 생성

```
POST /future-vision
Content-Type: multipart/form-data
```

**Request**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| weeklyVisionImageUrl | MultipartFile | O | 주간 비전 이미지 |
| yearlyVisionImageUrl | MultipartFile | O | 연간 비전 이미지 |
| yearlyVisionDescription | String | O | 연간 비전 설명 (최대 100자) |

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 409 | FUTURE_VISION_ALREADY_EXISTS | 이미 미래 비전이 존재 |

---

### 7.2 미래 비전 전체 조회

```
GET /future-vision
```

**Response** `200 OK`
```json
{
  "futureVisionId": "uuid",
  "weeklyVisionImageUrl": "https://...",
  "yearlyVisionImageUrl": "https://...",
  "yearlyVisionDescription": "연간 비전 설명",
  "yearlyVisionCreatedAt": "2026-03-21"
}
```

---

### 7.3 주간 비전 조회

```
GET /future-vision/weekly
```

**Response** `200 OK`
```json
{
  "futureVisionId": "uuid",
  "weeklyVisionImageUrl": "https://..."
}
```

---

### 7.4 연간 비전 조회

```
GET /future-vision/yearly
```

**Response** `200 OK`
```json
{
  "futureVisionId": "uuid",
  "yearlyVisionImageUrl": "https://...",
  "yearlyVisionDescription": "연간 비전 설명",
  "yearlyVisionCreatedAt": "2026-03-21"
}
```

---

### 7.5 주간 비전 수정

```
PATCH /future-vision/weekly
Content-Type: multipart/form-data
```

**Request**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| weeklyVisionImageUrl | MultipartFile | O | 새 주간 비전 이미지 |

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | FUTURE_VISION_NOT_FOUND | 미래 비전을 찾을 수 없음 |

---

### 7.6 연간 비전 수정

```
PATCH /future-vision/yearly
Content-Type: multipart/form-data
```

**Request**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| yearlyVisionImageUrl | MultipartFile | O | 새 연간 비전 이미지 |
| yearlyVisionDescription | String | O | 연간 비전 설명 (최대 100자) |

**Response**: `200 OK` (body 없음)

**에러**

| 상태 | 코드 | 설명 |
|------|------|------|
| 404 | FUTURE_VISION_NOT_FOUND | 미래 비전을 찾을 수 없음 |

---

## 8. 공통 에러 코드

| HTTP 상태 | 코드 | 메시지 |
|-----------|------|--------|
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류가 발생했습니다. |
| 401 | EXPIRED_JWT | JWT 토큰이 만료되었습니다. |
| 401 | INVALID_JWT | 유효하지 않은 JWT 토큰입니다. |
| 409 | DUPLICATE_ACCOUNT_ID | 이미 존재하는 계정 ID 입니다. |
| 409 | DUPLICATE_USER | 이미 존재하는 사용자입니다. |
| 409 | DUPLICATE_EMAIL | 이미 존재하는 이메일입니다. |
| 401 | LOGIN_FAILED | 로그인에 실패했습니다. |
| 404 | ACCOUNT_NOT_FOUND | 계정 ID를 찾을 수 없습니다. |
| 401 | PASSWORD_MISMATCH | 비밀번호가 일치하지 않습니다. |
| 404 | USER_NOT_FOUND | 사용자를 찾을 수 없습니다. |
| 500 | USER_CREATION_FAILED | 사용자 생성에 실패했습니다. |
| 404 | PENDING_OAUTH_NOT_FOUND | 대기 중인 OAuth 사용자를 찾을 수 없습니다. |
| 401 | REFRESH_TOKEN_NOT_FOUND | 리프레시 토큰을 찾을 수 없습니다. |
| 400 | IMAGE_NOT_FOUND | 이미지를 찾을 수 없습니다. |
| 400 | INVALID_FILE_EXTENSION | 지원하지 않는 파일 확장자입니다. |
| 500 | FILE_UPLOAD_FAILED | 파일 업로드에 실패했습니다. |
| 500 | FILE_DELETE_FAILED | 파일 삭제에 실패했습니다. |
| 409 | DAILY_PLAN_ALREADY_EXISTS | 해당 날짜의 데일리 플랜이 이미 존재합니다. |
| 404 | DAILY_PLAN_NOT_FOUND | 데일리 플랜을 찾을 수 없습니다. |
| 409 | FUTURE_VISION_ALREADY_EXISTS | 이미 미래 비전이 존재합니다. |
| 404 | FUTURE_VISION_NOT_FOUND | 미래 비전을 찾을 수 없습니다. |
| 500 | FUTURE_VISION_CREATION_FAILED | 미래 비전 생성에 실패했습니다. |
| 500 | FUTURE_VISION_UPDATE_FAILED | 미래 비전 수정에 실패했습니다. |
| 404 | TASK_NOT_FOUND | 작업을 찾을 수 없습니다. |
| 400 | INVALID_TASK_TYPE | 해당 작업 유형으로는 이 작업을 수행할 수 없습니다. |
| 400 | TASK_ALREADY_COMPLETED | 완료된 작업은 수정할 수 없습니다. |
| 400 | CORE_SELECTED_LIMIT_EXCEEDED | Top Pick 선택 개수를 초과했습니다. |
| 404 | TIMETABLE_NOT_FOUND | 타임테이블을 찾을 수 없습니다. |
| 404 | SLOT_NOT_FOUND | 슬롯을 찾을 수 없습니다. |
| 400 | TIME_OVERLAP | 다른 슬롯과 시간이 겹칩니다. |
| 400 | INVALID_TIME_RANGE | 유효하지 않은 시간 범위입니다. |
| 400 | TOP_PICKS_NOT_FULLY_ASSIGNED | 모든 Top Pick이 먼저 할당되어야 합니다. |
| 400 | TOP_PICK_SLOT_LIMIT_EXCEEDED | Top Pick 슬롯 개수를 초과했습니다. |
| 400 | NOT_TOP_PICKED_TASK | Top Pick으로 선택된 작업만 예상 시간을 수정할 수 있습니다. |
| 404 | TOP_PICK_DETAIL_NOT_FOUND | Top Pick 상세 정보를 찾을 수 없습니다. |

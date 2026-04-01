# 도메인 모델

## Bounded Context 개요

| BC | 핵심 애그리거트 | 역할 |
|----|----------------|------|
| Account | User | 인증, 사용자 프로필, OAuth 연동 |
| Planning | DailyPlan, Timetable | 일일 계획, 작업 관리, 시간표 |
| Study Room | Room, Participant | 스터디룸 생성/참여, 집중 타이머 |
| Feedback | TinyWin | 작은 성취 기록 |
| Visioning | FutureVision | 연간/주간 비전 설정 |

---

## Account BC

### User (애그리거트 루트)

```
User
├── UserId (VO, @EmbeddedId)
├── accountId (unique, 로컬 로그인용)
├── profileName
├── profileImage
├── passwordHash
└── List<AuthCredential> (1:N, cascade)
    ├── AuthCredentialId (VO)
    └── OauthProvider (GOOGLE, NAVER)
```

- 팩토리: `User.createLocalUser()`, `User.createOauthUser()`
- 소유권: `UserId`는 다른 BC에서 참조하는 글로벌 식별자

---

## Planning BC

### DailyPlan (애그리거트 루트)

```
DailyPlan
├── DailyPlanId (VO, @EmbeddedId)
├── UserId (소유자)
├── planDate
├── List<Task> (1:N, cascade, orphanRemoval)
│   ├── TaskId (VO, @EmbeddedId)
│   ├── TaskType (MORNING | MIND_SWEEP)
│   ├── title
│   ├── isCompleted
│   └── TopPickDetail (0..1, 선택적)
│       ├── TopPickDetailId (VO)
│       ├── estimatedMinutes
│       └── memo
└── unique: (userId, planDate)
```

- 팩토리: `DailyPlan.create(userId, planDate)`
- 제약: TopPick 최대 3개 (`MAX_CORE_SELECTED`)
- 소유권 검증: `DailyPlanRepository.findByDailyPlanIdAndUserId()`

**도메인 이벤트:**
- `DailyPlanCreatedEvent` → Timetable 자동 생성
- `TaskTopPickedEvent` → Timetable에 슬롯 예약
- `TaskTopPickUnpickedEvent` → Timetable 슬롯 제거

### Timetable (애그리거트 루트)

```
Timetable
├── TimetableId (VO, @EmbeddedId)
├── UserId (소유자)
├── DailyPlanId (연결)
└── List<Slot> (1:N, cascade, orphanRemoval)
    ├── SlotId (VO, @EmbeddedId)
    ├── startTime (LocalTime)
    ├── endTime (LocalTime)
    ├── taskId (선택적, Task 연결)
    └── SlotType (DIRECT | TASK_ASSIGNED | TOP_PICK)
```

- 시간 겹침 검증: `TimeOverlapException`
- DailyPlan과 도메인 이벤트로 동기화

**도메인 이벤트:**
- `SlotRescheduledEvent` → DailyPlan에 반영

---

## Study Room BC

### Room (추상 애그리거트 루트)

```
Room (abstract, JOINED 상속)
├── RoomId (VO, @EmbeddedId)
├── UserId leaderId (방장)
├── name (최대 20자)
├── createdAt
├── PublicRoom (서브클래스)
│   └── join() 그대로 사용
└── PrivateRoom (서브클래스)
    ├── InviteCode (VO)
    └── join() 오버라이드 → 초대 코드 검증
```

- 팩토리: `Room.create(name, userId, visibility)` → PublicRoom/PrivateRoom 분기
- 멤버십 추적 없음 (Participant가 SSOT)
- 방장 위임: `reassignLeader(newLeaderId)`

**핵심 설계 결정:**
- Room은 인가 정책만 담당 (kick, join 검증)
- 멤버십 데이터는 Participant 애그리거트가 소유
- join 로직: 다형성으로 처리 (instanceof 금지)

**도메인 이벤트:**
- `RoomCreatedEvent`
- `ParticipantJoinedEvent`
- `ParticipantKickedEvent`
- `RoomDissolvedEvent`

### Participant (애그리거트 루트)

```
Participant
├── ParticipantId (VO, @EmbeddedId)
├── UserId
├── RoomId
├── SessionMode (FOCUS | REST)
├── FocusTimer (VO, @Embedded, package-private)
│   ├── elapsedSeconds (누적 집중 시간)
│   └── startedAt
└── joinedAt
```

- 팩토리: `Participant.enter(userId, roomId)`
- 세션 전환: `startFocus()`, `takeBreak()`
- 퇴장: `leaveAndRecordTime()` → 집중 시간 기록 이벤트 발행

**FocusTimer 설계:**
- package-private 접근 제어 (Participant만 사용)
- null 값 없음: `elapsedSeconds`와 `startedAt` 항상 유효
- 상태 무관: Participant의 `SessionMode`가 상태 관리
- `start()` → 새 타이머 시작 (startedAt 갱신)
- `pause()` → 경과 시간 누적 후 새 타이머
- `totalElapsed()` → 누적만 (REST 상태용)
- `totalElapsedUntilNow()` → 누적 + 현재 경과 (FOCUS 상태용)

**도메인 이벤트:**
- `ParticipantLeftEvent`
- `FocusTimeRecordedEvent`

---

## Feedback BC

### TinyWin (애그리거트 루트)

```
TinyWin
├── TinyWinId (VO, @EmbeddedId)
├── UserId (소유자)
├── title
├── content
└── createdAt
```

---

## Visioning BC

### FutureVision (애그리거트 루트)

```
FutureVision
├── FutureVisionId (VO, @EmbeddedId)
├── UserId (소유자)
├── yearlyVision
└── weeklyVision
```

---

## 공통 패턴

### AbstractAggregateRoot

모든 애그리거트 루트의 베이스 클래스:

```java
public abstract class AbstractAggregateRoot {
    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event);  // 이벤트 등록

    @DomainEvents
    public List<Object> domainEvents();           // Spring Data 이벤트 발행

    @AfterDomainEventPublication
    public void clearDomainEvents();              // 발행 후 초기화
}
```

### Value Object (VO) 패턴

```java
@Embeddable
public record XxxId(UUID id) {
    public static XxxId newId() { return new XxxId(UUID.randomUUID()); }
    public static XxxId of(UUID id) { return new XxxId(id); }
}
```

### Helper 패턴

BC 내부에서 여러 서비스가 공유하는 조회 로직:

```
study_room/helper/StudyRoomReader.java  → Room, Participant 조회
visioning/future_vision/application/helper/FutureVisionPersister.java → 저장 헬퍼
```

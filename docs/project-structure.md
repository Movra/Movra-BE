# 프로젝트 구조

## 전체 패키지 구조

```
src/main/java/com/example/movra/
├── bc/                                    # Bounded Contexts
│   ├── account/                           # 인증/사용자 관리
│   │   ├── domain/
│   │   │   └── user/
│   │   │       ├── User.java              # 애그리거트 루트
│   │   │       ├── AuthCredential.java    # OAuth 인증 정보
│   │   │       ├── vo/                    # UserId, AuthCredentialId
│   │   │       ├── type/                  # OauthProvider
│   │   │       └── repository/            # UserRepository
│   │   ├── application/
│   │   │   ├── service/                   # Login, Signup, Token, OAuth 서비스
│   │   │   │   ├── dto/request/
│   │   │   │   └── dto/response/
│   │   │   └── exception/                 # AccountNotFound, DuplicateUser 등
│   │   ├── infrastructure/                # Account BC만 존재
│   │   │   ├── jwt/                       # JwtTokenProvider, JwtTokenFilter
│   │   │   ├── oauth/                     # Google, Naver OAuth 파서
│   │   │   ├── auth/                      # AuthDetails, AuthDetailsService
│   │   │   ├── current_user/              # CurrentUserService (CurrentUserQuery 구현)
│   │   │   └── token/                     # RefreshToken, RefreshTokenRepository
│   │   └── presentation/
│   │       └── AuthController.java
│   │
│   ├── planning/                          # 일일 계획
│   │   ├── daily_plan/                    # DailyPlan 애그리거트
│   │   │   ├── domain/
│   │   │   │   ├── DailyPlan.java         # 애그리거트 루트
│   │   │   │   ├── Task.java              # 엔티티 (DailyPlan 자식)
│   │   │   │   ├── TopPickDetail.java     # 엔티티 (Task 자식, 선택적)
│   │   │   │   ├── vo/                    # DailyPlanId, TaskId, TopPickDetailId
│   │   │   │   ├── type/                  # TaskType (MORNING, MIND_SWEEP)
│   │   │   │   ├── event/                 # DailyPlanCreated, TaskTopPicked 등
│   │   │   │   ├── exception/             # TaskNotFound, CoreSelectedLimit 등
│   │   │   │   └── repository/            # DailyPlanRepository
│   │   │   ├── application/
│   │   │   │   └── service/
│   │   │   │       ├── daily_plan/        # Create, Query
│   │   │   │       ├── mind_sweep/        # Add, Complete, Delete, Query, Update
│   │   │   │       ├── morning/           # Add, Complete, Delete, Query, Update
│   │   │   │       └── top_pick/          # Select, Deselect, Query
│   │   │   └── presentation/
│   │   │       ├── DailyPlanController.java
│   │   │       ├── MindSweepController.java
│   │   │       ├── MorningTaskController.java
│   │   │       └── TopPicksController.java
│   │   └── timetable/                     # Timetable 애그리거트
│   │       ├── domain/
│   │       │   ├── Timetable.java         # 애그리거트 루트
│   │       │   ├── Slot.java              # 엔티티 (Timetable 자식)
│   │       │   ├── vo/                    # TimetableId, SlotId
│   │       │   ├── event/                 # SlotRescheduledEvent
│   │       │   ├── exception/             # TimeOverlap, InvalidTimeRange 등
│   │       │   └── repository/            # TimetableRepository
│   │       ├── application/
│   │       │   └── service/               # AddDirectSlot, AssignTask, Reschedule 등
│   │       └── presentation/
│   │           ├── SlotController.java
│   │           └── TimetableController.java
│   │
│   ├── study_room/                        # 스터디룸
│   │   ├── helper/
│   │   │   └── StudyRoomReader.java       # Room/Participant 조회 헬퍼
│   │   ├── room/                          # Room 애그리거트
│   │   │   ├── domain/
│   │   │   │   ├── Room.java              # 추상 애그리거트 루트
│   │   │   │   ├── PublicRoom.java        # 공개 방
│   │   │   │   ├── PrivateRoom.java       # 비공개 방 (초대 코드)
│   │   │   │   ├── vo/                    # RoomId, InviteCode, Visibility
│   │   │   │   ├── event/                 # RoomCreated, ParticipantJoined 등
│   │   │   │   ├── exception/             # NotLeader, InvalidInviteCode 등
│   │   │   │   └── repository/            # RoomRepository
│   │   │   ├── application/
│   │   │   │   ├── service/               # Create, Join, Leave, Kick, Query
│   │   │   │   └── exception/             # RoomNotFoundException
│   │   │   └── presentation/
│   │   │       └── RoomController.java
│   │   └── participant/                   # Participant 애그리거트
│   │       ├── domain/
│   │       │   ├── Participant.java        # 애그리거트 루트
│   │       │   ├── FocusTimer.java         # VO (package-private)
│   │       │   ├── vo/                     # ParticipantId
│   │       │   ├── type/                   # SessionMode (FOCUS, REST)
│   │       │   ├── event/                  # ParticipantLeft, FocusTimeRecorded
│   │       │   └── exception/              # AlreadyFocusing, NotFocusing
│   │       ├── application/
│   │       │   ├── service/                # StartFocus, TakeBreak, Query
│   │       │   └── exception/              # ParticipantNotFoundException
│   │       └── presentation/
│   │           ├── ParticipantController.java
│   │           └── MyParticipationController.java
│   │
│   ├── feedback/                          # 피드백
│   │   └── tiny_win/                      # TinyWin 애그리거트
│   │       ├── domain/                    # TinyWin, TinyWinId
│   │       ├── application/               # Create, Query, Update, Delete
│   │       └── presentation/              # TinyWinController
│   │
│   └── visioning/                         # 비전
│       └── future_vision/                 # FutureVision 애그리거트
│           ├── domain/                    # FutureVision, FutureVisionId
│           ├── application/               # Create, Query, Update
│           └── presentation/              # FutureVisionController
│
├── sharedkernel/                          # 공통 모듈
│   ├── domain/
│   │   └── AbstractAggregateRoot.java     # 애그리거트 루트 베이스
│   ├── exception/
│   │   ├── ErrorCode.java                 # 전역 에러 코드 enum
│   │   ├── CustomException.java           # 예외 베이스 클래스
│   │   └── ErrorResponse.java             # 에러 응답 DTO
│   ├── user/
│   │   ├── CurrentUserQuery.java          # 현재 유저 조회 인터페이스
│   │   └── AuthenticatedUser.java         # 인증 유저 record
│   ├── file/storage/                      # 파일 업로드/삭제
│   └── validation/                        # 커스텀 Validation
│
└── config/                                # 글로벌 설정
    ├── security/SecurityConfig.java       # Spring Security + JWT + OAuth2
    ├── exception/
    │   ├── GlobalExceptionHandler.java    # @ControllerAdvice
    │   └── GlobalExceptionFilter.java
    └── file/S3Config.java                 # S3/SeaweedFS 설정
```

## 레이어 구조

각 BC의 서브도메인은 다음 레이어로 구성:

| 레이어 | 역할 | 위치 |
|--------|------|------|
| `domain/` | 엔티티, VO, 리포지토리 인터페이스, 도메인 이벤트, 도메인 예외 | `bc/{bc}/{subdomain}/domain/` |
| `application/` | 서비스, DTO, 애플리케이션 예외, 이벤트 핸들러 | `bc/{bc}/{subdomain}/application/` |
| `presentation/` | 컨트롤러 (REST API) | `bc/{bc}/{subdomain}/presentation/` |
| `infrastructure/` | 외부 시스템 연동 (Account BC만) | `bc/account/infrastructure/` |

## Bounded Context 간 통신

- BC 간 직접 참조 금지
- **도메인 이벤트**를 통해서만 통신
- 예: `DailyPlanCreatedEvent` → `DailyPlanCreatedEventHandler` (timetable BC에서 처리)
- 예: `TaskTopPickedEvent` → `TaskTopPickedEventHandler`
- 예외: `UserId`는 sharedkernel이 아닌 Account BC에 위치하나, 다른 BC에서 참조 허용

## 테스트 구조

```
src/test/java/com/example/movra/
├── application/
│   ├── account/user/              # Account 서비스 테스트
│   ├── planning/
│   │   ├── daily_plan/            # DailyPlan 서비스 테스트
│   │   └── timetable/             # Timetable 서비스 테스트
│   ├── study_room/
│   │   ├── room/                  # Room 서비스 테스트
│   │   └── participant/           # Participant 서비스 테스트
│   └── visioning/future_vision/   # FutureVision 서비스 테스트
└── MorvaApplicationTests.java     # 통합 테스트
```

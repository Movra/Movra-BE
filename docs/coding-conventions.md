# 코딩 컨벤션

## DDD 패턴

### 애그리거트 루트

```java
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_xxx")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Xxx extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "xxx_id"))
    private XxxId id;

    public static Xxx create(...) {
        Xxx entity = Xxx.builder()
                .id(XxxId.newId())
                // ...
                .build();
        entity.registerEvent(new XxxCreatedEvent(...));
        return entity;
    }
}
```

**규칙:**
- `AbstractAggregateRoot` 상속 필수
- 생성자 직접 호출 금지 → `Entity.create(...)` 정적 팩토리 메서드
- `@Builder(access = PRIVATE)` + `@NoArgsConstructor(access = PROTECTED)`
- 테이블명: `tbl_` 접두사

### Value Object (VO)

```java
@Embeddable
public record XxxId(UUID id) {
    public static XxxId newId() { return new XxxId(UUID.randomUUID()); }
    public static XxxId of(UUID id) { return new XxxId(id); }
}
```

- `@EmbeddedId` 또는 `@Embedded`로 사용
- `@AttributeOverride`로 컬럼명 매핑
- 불변 객체 (record 또는 final 필드)

### 도메인 이벤트

```java
// 이벤트 정의
public record XxxCreatedEvent(XxxId id) {}

// 이벤트 발행 (애그리거트 내부)
registerEvent(new XxxCreatedEvent(this.id));

// 이벤트 처리
@Component
@RequiredArgsConstructor
public class XxxCreatedEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(XxxCreatedEvent event) { ... }
}
```

---

## 서비스 레이어

### Command 서비스

```java
@Service
@RequiredArgsConstructor
public class CreateXxxService {

    private final XxxRepository xxxRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public XxxResponse create(CreateXxxRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        Xxx entity = Xxx.create(userId, request.name());
        xxxRepository.save(entity);
        return XxxResponse.from(entity);
    }
}
```

**규칙:**
- `@Transactional` 필수
- 단일 책임: `CreateXxxService`, `UpdateXxxService`, `DeleteXxxService`
- 현재 유저: `currentUserQuery.currentUser().userId()`
- 반환: `void` 또는 Response DTO

### Query 서비스

```java
@Service
@RequiredArgsConstructor
public class QueryXxxService {

    private final XxxRepository xxxRepository;

    @Transactional(readOnly = true)
    public XxxResponse query(UUID id) {
        Xxx entity = xxxRepository.findById(XxxId.of(id))
                .orElseThrow(XxxNotFoundException::new);
        return XxxResponse.from(entity);
    }
}
```

**규칙:**
- `@Transactional(readOnly = true)` 필수
- DTO 또는 List로 반환

### Helper / Reader

BC 내부에서 여러 서비스가 공유하는 조회 로직:

```java
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomReader {

    private final RoomRepository roomRepository;

    public Room getRoom(UUID roomId) {
        return roomRepository.findById(RoomId.of(roomId))
                .orElseThrow(RoomNotFoundException::new);
    }
}
```

- `helper/` 패키지에 위치 (서비스 패키지와 분리)
- `@Component` 사용 (`@Service` 아님)

---

## DTO

### Request

```java
public record CreateXxxRequest(
        @NotBlank String name,
        @NotNull Visibility visibility
) {}
```

- `record` 타입
- `application/service/dto/request/` 패키지

### Response

```java
@Builder
public record XxxResponse(
        UUID id,
        String name
) {
    public static XxxResponse from(Xxx entity) {
        return XxxResponse.builder()
                .id(entity.getId().id())
                .name(entity.getName())
                .build();
    }
}
```

- `record` + `@Builder` + `from()` 팩토리
- `application/service/dto/response/` 패키지

---

## 예외 처리

### ErrorCode

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    XXX_NOT_FOUND(HttpStatus.NOT_FOUND, "Xxx was not found."),
    INVALID_XXX(HttpStatus.BAD_REQUEST, "Invalid xxx.");

    private final HttpStatus httpStatus;
    private final String message;
}
```

- 모든 비즈니스 예외의 응답 기준
- HTTP 상태 코드와 기본 메시지를 중앙에서 관리
- 메시지는 영문으로 통일
- `NOT_FOUND`: 조회 실패
- `BAD_REQUEST`: 비즈니스 규칙 위반
- `CONFLICT`: 중복
- `FORBIDDEN`: 권한 부족

### CustomException

```java
@Getter
public abstract class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    protected CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 구체 예외 클래스

```java
public class XxxNotFoundException extends CustomException {
    public XxxNotFoundException() {
        super(ErrorCode.XXX_NOT_FOUND);
    }
}
```

- 예외 클래스는 얇게 유지
- 예외의 의미는 클래스 이름으로 표현
- 상태 코드와 메시지는 `ErrorCode`에 위임

**예외 위치 규칙:**
- 도메인 규칙 위반, 불변식 검증 → `domain/exception/`
  - 예: `AlreadyFocusingException`, `LeaderCannotKickSelfException`
- 조회 실패, 유스케이스 처리 실패 → `application/exception/`
  - 예: `RoomNotFoundException`, `ParticipantNotFoundException`
- JWT, 파일, 외부 시스템 관련 실패 → `infrastructure/.../exception/` 또는 `sharedkernel/.../exception/`
  - 예: `InvalidJwtException`, `InvalidFileExtensionException`

**사용 규칙:**
- 비즈니스 실패는 `CustomException` 계열 사용
- 내부 검증, 잘못된 인자, 개발자 오류는 `IllegalArgumentException` 사용 가능
- 최종 응답 변환은 전역 예외 처리기에서 담당

---

## 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|------|------|------|
| 패키지 | snake_case | `daily_plan`, `study_room`, `tiny_win` |
| 클래스 | PascalCase + 역할 접미사 | `CreateRoomService`, `RoomController` |
| VO 식별자 | `XxxId` | `UserId`, `RoomId`, `ParticipantId` |
| 리포지토리 | `XxxRepository` | `RoomRepository`, `DailyPlanRepository` |
| 컨트롤러 | `XxxController` | `RoomController`, `ParticipantController` |
| 서비스 (명령) | `동사XxxService` | `CreateRoomService`, `JoinRoomService` |
| 서비스 (조회) | `QueryXxxService` | `QueryRoomService` |
| 예외 | `Xxx(Not Found/Already)Exception` | `RoomNotFoundException` |
| 요청 DTO | `XxxRequest` | `CreateRoomRequest` |
| 응답 DTO | `XxxResponse` | `RoomDetailResponse` |
| 테스트 | `XxxServiceTest` | `CreateRoomServiceTest` |
| 테스트 메서드 | `method_condition_result` | `create_publicRoom_success` |

---

## 컨트롤러

```java
@RestController
@RequestMapping("/xxx")
@RequiredArgsConstructor
public class XxxController {

    private final CreateXxxService createXxxService;
    private final QueryXxxService queryXxxService;

    @PostMapping
    public XxxResponse create(@Valid @RequestBody CreateXxxRequest request) {
        return createXxxService.create(request);
    }

    @GetMapping("/{id}")
    public XxxResponse query(@PathVariable UUID id) {
        return queryXxxService.query(id);
    }
}
```

**규칙:**
- `@RestController` + `@RequestMapping`
- `@Valid` 로 요청 검증
- 서비스에 위임만 수행 (로직 없음)

---

## 리포지토리

```java
@Repository
public interface XxxRepository extends JpaRepository<Xxx, XxxId> {
    Optional<Xxx> findByXxxIdAndUserId(XxxId id, UserId userId);  // 소유권 검증
}
```

- `domain/repository/` 패키지에 위치
- JPA 인터페이스만 정의
- 소유권 검증 메서드 포함

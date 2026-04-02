# 테스트 가이드

## 테스트 전략

- **단위 테스트 중심**: 서비스 레이어 단위 테스트
- **Mockito**: 의존성 모킹
- **AssertJ**: 가독성 높은 assertion
- **BDD 스타일**: given / when / then 구조

## 테스트 패턴

### 기본 구조

```java
@ExtendWith(MockitoExtension.class)
class CreateXxxServiceTest {

    @InjectMocks
    private CreateXxxService createXxxService;

    @Mock
    private XxxRepository xxxRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    // 유저 모킹 헬퍼 (lenient 사용)
    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("생성 성공")
    void create_success() {
        // given
        givenCurrentUser();

        // when
        XxxResponse response = createXxxService.create(new CreateXxxRequest("이름"));

        // then
        assertThat(response.id()).isNotNull();
        then(xxxRepository).should().save(any());
    }

    @Test
    @DisplayName("예외 상황 테스트")
    void create_duplicated_throwsException() {
        // given
        givenCurrentUser();
        given(xxxRepository.existsById(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> createXxxService.create(request))
                .isInstanceOf(XxxAlreadyExistsException.class);
    }
}
```

### 컨벤션

| 항목 | 규칙 |
|------|------|
| 어노테이션 | `@ExtendWith(MockitoExtension.class)` |
| 의존성 주입 | `@InjectMocks` (테스트 대상), `@Mock` (의존성) |
| 유저 모킹 | `givenCurrentUser()` 헬퍼 메서드, `lenient()` 사용 |
| 테스트 이름 | `@DisplayName("한국어 설명")` |
| 메서드명 | `methodName_condition_expectedResult` |
| given 설정 | `given(mock.method()).willReturn(value)` (BDD) |
| then 검증 | `then(mock).should().method()` (BDD) |
| Assertion | AssertJ (`assertThat`, `assertThatThrownBy`) |

### Mockito 사용 패턴

```java
// BDD given
given(repository.findById(any())).willReturn(Optional.of(entity));

// BDD then (호출 검증)
then(repository).should().save(any());

// lenient (불필요 스터빙 경고 방지)
lenient().when(currentUserQuery.currentUser()).thenReturn(...);

// 예외 검증
assertThatThrownBy(() -> service.execute())
        .isInstanceOf(XxxNotFoundException.class);
```

## 테스트 파일 위치

```
src/test/java/com/example/movra/application/{bc}/{subdomain}/
```

- 메인 코드의 BC 구조를 미러링
- `application/` 패키지 아래에 서비스 테스트 배치

## 테스트 실행

```bash
# 전체 테스트
./gradlew test

# BC별 테스트
./gradlew test --tests "com.example.movra.application.planning.*"
./gradlew test --tests "com.example.movra.application.study_room.*"
./gradlew test --tests "com.example.movra.application.account.*"
./gradlew test --tests "com.example.movra.application.visioning.*"

# 특정 테스트 클래스
./gradlew test --tests "com.example.movra.application.study_room.room.CreateRoomServiceTest"

# 전체 빌드 (컴파일 + 테스트)
./gradlew build

# 컴파일만 (테스트 제외)
./gradlew compileJava
./gradlew compileTestJava
```

## 테스트 환경

- DB: H2 인메모리 (`create-drop`)
- 설정: `src/test/resources/application.yml`
- JWT: 테스트용 시크릿 키

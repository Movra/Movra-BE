# Testing Guide

## Principles

- Prefer unit tests for application services.
- Use Mockito for dependencies.
- Use AssertJ for readable assertions.
- Follow `given / when / then`.

## Template

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

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    void create_success() {
        givenCurrentUser();

        XxxResponse response = createXxxService.create(new CreateXxxRequest("name"));

        assertThat(response.id()).isNotNull();
        then(xxxRepository).should().save(any());
    }

    @Test
    void create_duplicated_throwsException() {
        givenCurrentUser();
        given(xxxRepository.existsById(any())).willReturn(true);

        assertThatThrownBy(() -> createXxxService.create(new CreateXxxRequest("name")))
                .isInstanceOf(XxxAlreadyExistsException.class);
    }
}
```

## Conventions

- Use `@ExtendWith(MockitoExtension.class)`.
- Use `@InjectMocks` for the target service and `@Mock` for collaborators.
- Keep a `givenCurrentUser()` helper when `CurrentUserQuery` is used.
- Use method names like `method_condition_expectedResult`.
- Use BDD Mockito style when possible.

## Test Location

```text
src/test/java/com/example/movra/application/{bc}/{subdomain}/
```

## Commands

```bash
./gradlew test
./gradlew build
./gradlew compileJava
./gradlew compileTestJava
```

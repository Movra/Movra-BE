# Project Structure

## Main Layout

```text
src/main/java/com/example/movra/
  bc/
    account/
    planning/
    study_room/
    feedback/
    visioning/
  sharedkernel/
  config/
```

## Layer Rules

| Layer | Responsibility | Location |
|------|----------------|----------|
| `domain/` | Entity, VO, repository interface, domain event, domain exception | `bc/{bc}/{subdomain}/domain/` |
| `application/` | Service, DTO, application exception, event handler | `bc/{bc}/{subdomain}/application/` |
| `presentation/` | REST controller | `bc/{bc}/{subdomain}/presentation/` |
| `infrastructure/` | External system integration | `bc/account/infrastructure/` |

## Study Room

```text
bc/study_room/
  helper/
    StudyRoomReader.java
  room/
    domain/
    application/
    presentation/
  participant/
    domain/
    application/
    presentation/
```

## Shared Kernel

```text
sharedkernel/
  domain/
    AbstractAggregateRoot.java
  exception/
  user/
  file/storage/
  validation/
```

## Tests

```text
src/test/java/com/example/movra/
  application/
    account/
    planning/
    study_room/
    visioning/
  MovraApplicationTests.java
```

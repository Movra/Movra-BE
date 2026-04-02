# Git 워크플로우

## 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 배포 브랜치 |
| `feat/xxx` | 기능 개발 브랜치 |

## 커밋 메시지

```text
<type>: <한국어 설명>
```

### Type

| Type | 용도 | 예시 |
|------|------|------|
| `feat` | 새 기능 추가 | `feat: 스터디룸 생성 서비스 구현` |
| `fix` | 버그 수정 | `fix: 집중 타이머 시간 계산 오류 수정` |
| `refactor` | 리팩토링 | `refactor: Room 애그리거트 멤버십 제거` |
| `test` | 테스트 추가/수정 | `test: CreateRoomService 유닛 테스트 추가` |
| `chore` | 설정, 의존성 등 | `chore: Spring Boot 버전 업그레이드` |

### 규칙

- 설명은 **한국어**로 작성
- 기능별로 커밋을 **나누어** 작성
- 한번에 몰아서 커밋하지 않음
- 커밋 단위: 하나의 논리적 변경 (서비스 1개, 테스트 1개 등)

### 예시

```text
feat: Room 도메인 모델 구현
feat: Participant 도메인 모델 구현
feat: 스터디룸 서비스 레이어 구현
test: 스터디룸 서비스 유닛 테스트 추가
refactor: FocusTimer 상태 모델 단순화
fix: CurrentUserQuery import 경로 수정
```

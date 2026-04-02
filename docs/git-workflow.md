# Git Workflow

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Release branch |
| `feat/xxx` | Feature development branch |

## Commit Message

```text
<type>: <short summary>
```

### Types

| Type | Purpose | Example |
|------|---------|---------|
| `feat` | Add feature | `feat: implement study room creation service` |
| `fix` | Fix bug | `fix: correct focus time calculation` |
| `refactor` | Refactor code | `refactor: simplify room aggregate members` |
| `test` | Add or update tests | `test: add CreateRoomService tests` |
| `chore` | Config or dependency change | `chore: upgrade Spring Boot version` |

### Rules

- Write the summary in imperative mood.
- Keep each commit focused on one logical change.
- Do not bundle unrelated changes into one commit.
- Prefer a small commit scope that is easy to review.

### Examples

```text
feat: implement Room domain model
feat: implement Participant domain model
feat: implement study room service layer
test: add study room service unit tests
refactor: simplify FocusTimer state model
fix: correct CurrentUserQuery import path
```

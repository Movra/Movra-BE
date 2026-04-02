# 기술 스택

## 핵심 기술

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 | 메인 언어 (Virtual Threads 활성화) |
| Spring Boot | 3.5.11 | 프레임워크 |
| MySQL | 9.3.0 (Connector) | 메인 DB |
| Hibernate | (Spring Boot 관리) | ORM, DDL auto-update |

## 의존성

### Spring Boot Starters

| Starter | 용도 |
|---------|------|
| `spring-boot-starter-data-jpa` | JPA/Hibernate |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-oauth2-client` | OAuth2 (Google, Naver) |
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-data-redis` | Redis (세션/캐시) |
| `spring-boot-starter-mail` | 이메일 발송 |
| `spring-boot-starter-amqp` | RabbitMQ |
| `spring-boot-starter-websocket` | WebSocket |

### 인증/보안

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `jjwt-api` | 0.11.5 | JWT 토큰 생성/검증 |
| `jjwt-impl` | 0.11.5 | JWT 구현체 |
| `jjwt-jackson` | 0.11.5 | JWT JSON 처리 |

### 파일 저장

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `aws-sdk-s3` | 2.20.40 | SeaweedFS (S3 호환) 파일 업로드 |

### 개발 도구

| 라이브러리 | 용도 |
|-----------|------|
| `lombok` | 보일러플레이트 제거 (compileOnly) |

### 테스트

| 라이브러리 | 용도 |
|-----------|------|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |
| `spring-security-test` | Security 테스트 |
| `archunit-junit5` (1.2.1) | 아키텍처 테스트 |
| `spring-rabbit-test` | RabbitMQ 테스트 |
| `awaitility` (4.2.0) | 비동기 테스트 |
| `h2` | 인메모리 테스트 DB |

## 인프라 설정

### application.yml 주요 설정

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  # Virtual Threads 활성화
server:
  tomcat:
    threads:
      virtual:
        enabled: true
```

### 환경변수

| 변수 | 용도 |
|------|------|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MySQL 연결 |
| `DB_USERNAME`, `DB_PASSWORD` | DB 인증 |
| `JWT_SECRET` | JWT 시크릿 키 |
| `JWT_ACCESS_EXP`, `JWT_REFRESH_EXP` | 토큰 만료 시간 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google OAuth |
| `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` | Naver OAuth |
| `SEAWEEDFS_*` | 파일 스토리지 |

## 빌드

```bash
./gradlew build          # 전체 빌드 (컴파일 + 테스트)
./gradlew test           # 테스트만
./gradlew compileJava    # 컴파일만
./gradlew bootRun        # 로컬 실행
```

# Tech Stack

## Core

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Main language |
| Spring Boot | 3.5.11 | Application framework |
| MySQL Connector | 9.3.0 | Main database driver |
| Hibernate | Managed by Spring Boot | ORM |

## Main Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-data-jpa` | JPA and Hibernate |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-oauth2-client` | OAuth2 login |
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-data-redis` | Redis |
| `spring-boot-starter-mail` | Mail sending |
| `spring-boot-starter-amqp` | RabbitMQ |
| `spring-boot-starter-websocket` | WebSocket |
| `aws-sdk-s3` | S3 compatible file storage |
| `lombok` | Boilerplate reduction |

## Test Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |
| `spring-security-test` | Security tests |
| `archunit-junit5` | Architecture tests |
| `spring-rabbit-test` | RabbitMQ tests |
| `awaitility` | Async tests |
| `h2` | In-memory test DB |

## Config Notes

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  tomcat:
    threads:
      virtual:
        enabled: true
```

## Build

```bash
./gradlew build
./gradlew test
./gradlew compileJava
./gradlew bootRun
```

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * GET /focus-statistics/monthly 동시 부하 한계 탐색 시뮬레이션.
 *
 * 단일 유저 최적화(N→1 쿼리) 이후 동시 트래픽 한계를 측정한다.
 * 3단계 부하를 순차 주입해 어느 시점에서 p95가 급등하거나 실패가 발생하는지 관찰한다.
 *
 * Stage 1: 1명 동시 (warm-up 확인)
 * Stage 2: 5명 동시 (일반 트래픽 가정)
 * Stage 3: 10명 동시 (스파이크)
 *
 * 각 단계 사이 10초 대기로 이전 단계 처리가 완료되게 한다.
 *
 * 관찰 포인트:
 * - p95, p99 급등 구간
 * - HikariCP 풀 고갈 시 "Connection is not available" 에러 로그
 * - 실패율이 1% 초과하는 동시 사용자 수
 *
 * Run:
 *   AUTH_TOKEN="<JWT>" ./gradlew gatlingRun \
 *       -DsimulationClass=FocusStatisticsConcurrentSimulation \
 *       -DbaseUrl=http://localhost:8080 \
 *       -DtargetDate=2026-04-01
 */
public class FocusStatisticsConcurrentSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String AUTH_TOKEN = resolveAuthToken();
    private static final String TARGET_DATE = System.getProperty("targetDate", "2026-04-01");

    private static String resolveAuthToken() {
        String fromEnv = System.getenv("AUTH_TOKEN");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return System.getProperty("authToken", "");
    }

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .header("Authorization", authorizationHeader())
            .shareConnections();

    // monthly: rawPeriods 가 가장 많이 생기는 시나리오 (최악 케이스)
    private final ScenarioBuilder monthlyScn = scenario("GET /focus-statistics/monthly concurrent")
            .exec(http("monthly")
                    .get("/focus-statistics/monthly?targetDate=" + TARGET_DATE)
                    .check(status().is(200)));

    {
        if (AUTH_TOKEN.isBlank()) {
            throw new IllegalStateException(
                    "Auth token is required. Set AUTH_TOKEN env var or pass -DauthToken=<JWT>.");
        }

        // 단일 시나리오에 다단계 주입 — 시나리오 이름 중복 방지
        // Stage 1 (0s)  : 1명 즉시
        // Stage 2 (10s) : 5명 즉시
        // Stage 3 (20s) : 10명 즉시 (스파이크)
        // Stage 4 (30s) : 5초에 걸쳐 20명까지 점진 증가 (한계 탐색)
        setUp(
                monthlyScn.injectOpen(
                        atOnceUsers(1),
                        nothingFor(Duration.ofSeconds(10)),
                        atOnceUsers(5),
                        nothingFor(Duration.ofSeconds(10)),
                        atOnceUsers(10),
                        nothingFor(Duration.ofSeconds(10)),
                        rampUsers(20).during(Duration.ofSeconds(5))
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(2000),
                        global().responseTime().percentile4().lt(5000),
                        global().failedRequests().percent().lt(5.0)
                );
    }

    private static String authorizationHeader() {
        return AUTH_TOKEN.startsWith("Bearer ") ? AUTH_TOKEN : "Bearer " + AUTH_TOKEN;
    }
}

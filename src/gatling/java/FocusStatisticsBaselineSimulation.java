import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * GET /focus-statistics/{daily,weekly,monthly} single-user sequential baseline.
 *
 * 성능 개선 전/후 비교를 위한 베이스라인 측정 시뮬레이션.
 * monthly 쿼리가 핵심 측정 대상: rawPeriods N개 → N번 개별 DB 조회 문제.
 *
 * Run:
 *   AUTH_TOKEN="<JWT>" ./gradlew gatlingRun \
 *       -DsimulationClass=FocusStatisticsBaselineSimulation \
 *       -DbaseUrl=http://localhost:8080 \
 *       -DtargetDate=2026-05-15 \
 *       -Diterations=30 -DpauseMs=500
 */
public class FocusStatisticsBaselineSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String AUTH_TOKEN = resolveAuthToken();
    private static final String TARGET_DATE = System.getProperty("targetDate", "2026-05-15");
    private static final int ITERATIONS = Integer.parseInt(System.getProperty("iterations", "30"));
    private static final int PAUSE_MS = Integer.parseInt(System.getProperty("pauseMs", "500"));
    private static final int WARMUP_ITERATIONS = Integer.parseInt(System.getProperty("warmupIterations", "3"));

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

    // monthly: rawPeriods가 가장 많이 생기는 시나리오 (최대 ~16개 쿼리)
    private final ScenarioBuilder monthlyScn = scenario("GET /focus-statistics/monthly sequential")
            .exec(repeat(WARMUP_ITERATIONS, "warmup").on(
                    exec(http("warmup_monthly")
                            .get("/focus-statistics/monthly?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ))
            .exec(repeat(ITERATIONS, "measured").on(
                    exec(http("monthly")
                            .get("/focus-statistics/monthly?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ));

    // weekly: 최대 7일 범위 (rawPeriods 최대 ~4개)
    private final ScenarioBuilder weeklyScn = scenario("GET /focus-statistics/weekly sequential")
            .exec(repeat(WARMUP_ITERATIONS, "warmup").on(
                    exec(http("warmup_weekly")
                            .get("/focus-statistics/weekly?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ))
            .exec(repeat(ITERATIONS, "measured").on(
                    exec(http("weekly")
                            .get("/focus-statistics/weekly?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ));

    // daily: 단 1일 범위 (rawPeriods 최대 1개) — 비교 기준점
    private final ScenarioBuilder dailyScn = scenario("GET /focus-statistics/daily sequential")
            .exec(repeat(WARMUP_ITERATIONS, "warmup").on(
                    exec(http("warmup_daily")
                            .get("/focus-statistics/daily?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ))
            .exec(repeat(ITERATIONS, "measured").on(
                    exec(http("daily")
                            .get("/focus-statistics/daily?targetDate=" + TARGET_DATE)
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ));

    {
        if (AUTH_TOKEN.isBlank()) {
            throw new IllegalStateException(
                    "Auth token is required. Set AUTH_TOKEN env var (preferred) or pass -DauthToken=<JWT>.");
        }

        setUp(
                monthlyScn.injectOpen(atOnceUsers(1)),
                weeklyScn.injectOpen(atOnceUsers(1)),
                dailyScn.injectOpen(atOnceUsers(1))
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(2000),
                        global().responseTime().percentile4().lt(4000),
                        global().failedRequests().percent().lt(1.0)
                );
    }

    private static String authorizationHeader() {
        return AUTH_TOKEN.startsWith("Bearer ") ? AUTH_TOKEN : "Bearer " + AUTH_TOKEN;
    }
}

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
 * GET /home/today single-user sequential baseline.
 *
 * Closed model: 1 virtual user issues N sequential requests, each starting
 * after the previous finishes. Measures pure server-side processing latency,
 * no queueing effects.
 *
 * Run (env var preferred; -DauthToken fallback only):
 *   AUTH_TOKEN="<JWT>" ./gradlew gatlingRun \
 *       -DbaseUrl=http://localhost:8080 \
 *       -Diterations=30 -DpauseMs=500
 */
public class HomeTodayBaselineSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String AUTH_TOKEN = resolveAuthToken();
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

    private final ScenarioBuilder scn = scenario("GET /home/today sequential")
            .exec(repeat(WARMUP_ITERATIONS, "warmup").on(
                    exec(http("warmup_home_today")
                            .get("/home/today")
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ))
            .exec(repeat(ITERATIONS, "measured").on(
                    exec(http("home_today")
                            .get("/home/today")
                            .check(status().is(200))),
                    pause(Duration.ofMillis(PAUSE_MS))
            ));

    {
        if (AUTH_TOKEN.isBlank()) {
            throw new IllegalStateException(
                    "Auth token is required. Set AUTH_TOKEN env var (preferred) or pass -DauthToken=<JWT>.");
        }

        setUp(scn.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(1000),
                        global().responseTime().percentile4().lt(2000),
                        global().failedRequests().percent().lt(1.0)
                );
    }

    private static String authorizationHeader() {
        return AUTH_TOKEN.startsWith("Bearer ") ? AUTH_TOKEN : "Bearer " + AUTH_TOKEN;
    }
}

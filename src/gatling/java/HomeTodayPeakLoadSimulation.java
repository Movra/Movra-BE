import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * GET /home/today sustained peak-load simulation.
 *
 * Models the stated production target: DAU 150, 5pm peak, ~10 TPS, read-heavy.
 * Open model — virtual users arrive at a fixed rate, each firing one request,
 * so arrival rate equals requests-per-second regardless of server latency.
 *
 *   warmup : 2 req/s for 10s
 *   ramp   : 1 -> targetTps req/s over rampSeconds
 *   hold   : targetTps req/s for holdSeconds
 *
 * Run:
 *   ./gradlew gatlingRun --simulation HomeTodayPeakLoadSimulation --non-interactive \
 *       -DauthToken=<JWT> -DbaseUrl=http://localhost:8080 \
 *       -DtargetTps=10 -DrampSeconds=30 -DholdSeconds=120
 */
public class HomeTodayPeakLoadSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String AUTH_TOKEN = resolveAuthToken();
    private static final int TARGET_TPS = positiveIntProperty("targetTps", 10);
    private static final int RAMP_SECONDS = positiveIntProperty("rampSeconds", 30);
    private static final int HOLD_SECONDS = positiveIntProperty("holdSeconds", 120);

    private static String resolveAuthToken() {
        String fromEnv = System.getenv("AUTH_TOKEN");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return System.getProperty("authToken", "");
    }

    private static int positiveIntProperty(String propertyName, int defaultValue) {
        String rawValue = System.getProperty(propertyName, Integer.toString(defaultValue));

        try {
            int value = Integer.parseInt(rawValue);
            if (value <= 0) {
                throw new IllegalArgumentException(propertyName + " must be a positive integer, but was: " + rawValue);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(propertyName + " must be a positive integer, but was: " + rawValue, e);
        }
    }

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .header("Authorization", authorizationHeader())
            .shareConnections();

    private final ScenarioBuilder scn = scenario("GET /home/today peak load")
            .exec(http("home_today")
                    .get("/home/today")
                    .check(status().is(200)));

    {
        if (AUTH_TOKEN.isBlank()) {
            throw new IllegalStateException(
                    "Auth token is required. Set AUTH_TOKEN env var (preferred) or pass -DauthToken=<JWT>.");
        }

        setUp(scn.injectOpen(
                        constantUsersPerSec(2).during(Duration.ofSeconds(10)),
                        rampUsersPerSec(1).to(TARGET_TPS).during(Duration.ofSeconds(RAMP_SECONDS)),
                        constantUsersPerSec(TARGET_TPS).during(Duration.ofSeconds(HOLD_SECONDS))
                ))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(5.0)
                );
    }

    private static String authorizationHeader() {
        return AUTH_TOKEN.startsWith("Bearer ") ? AUTH_TOKEN : "Bearer " + AUTH_TOKEN;
    }
}

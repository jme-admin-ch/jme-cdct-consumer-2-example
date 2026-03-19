package ch.admin.bit.jeap.jme.cdct.consumer.it;

import ch.admin.bit.jeap.jme.cdct.ConsumerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * This is an integration test for the CDCT Consumer Example itself.
 * For an example of how to implement a CDC Consumer Test, see TaskClientConsumerPactTest.
 */
@SpringBootTest(classes = ConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class CdctConsumerApplicationLocalProfileIT {

    private static final String HEALTH_URL = "http://localhost:%d/jme-cdct-consumer-2-service/actuator/health/readiness";

    @LocalServerPort
    private int port;

    @Test
    void readinessCheckIsUp() {
        await().atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        given()
                                .when()
                                .get(HEALTH_URL.formatted(port))
                                .then()
                                .statusCode(200)
                                .body("status", equalTo("UP"))
                );
    }
}

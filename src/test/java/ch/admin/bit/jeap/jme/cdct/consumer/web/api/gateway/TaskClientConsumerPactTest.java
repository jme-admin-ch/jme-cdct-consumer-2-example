package ch.admin.bit.jeap.jme.cdct.consumer.web.api.gateway;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task.Task;
import ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task.TaskClient;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationContext;
import ch.admin.bit.jeap.security.test.client.MockJeapOAuth2RestClientBuilderFactory;
import ch.admin.bit.jeap.security.test.client.configuration.JeapOAuth2IntegrationTestClientConfiguration;
import ch.admin.bit.jeap.security.test.jws.JwsBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test class specifies this consumer's pact (contracts) for its provider and checks that the consumer
 * can interact successfully with a provider that complies to that pact (consumer tests).
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "unused", "SpringBootApplicationProperties"})
@PactConsumerTest
@PactTestFor(pactVersion = PactSpecVersion.V4)
@MockServerConfig(hostInterface = "localhost", port = "8888")
@Import(JeapOAuth2IntegrationTestClientConfiguration.class)
@SpringBootTest(properties = {"jme.cdct-consumer-2.task-api-url=http://localhost:8888/api/task"})
class TaskClientConsumerPactTest {

    private static final String CONSUMER = "bit-jme-cdct-consumer-service-2";
    private static final String PROVIDER = "bit-jme-cdct-provider-service";

    private static final String API_PATH = "/api/task";

    private static final String ID_FIELD_NAME = "id";
    private static final String TITLE_FIELD_NAME = "title";
    private static final String CONTENT_FIELD_NAME = "content";

    private static final String ID_EXAMPLE_VALUE = "123456789";
    private static final String TITLE_EXAMPLE_VALUE = "test-title";
    private static final String CONTENT_EXAMPLE_VALUE = "test-content";

    private static final String TASK_ID_PARAM_NAME = "task-id";

    @Autowired
    private TaskClient taskClient;

    @Autowired
    private MockJeapOAuth2RestClientBuilderFactory mockRestClientBuilderFactory;

    private String taskReadToken;
    private String unrelatedRoleToken;

    @BeforeEach
    void init(@Autowired JwsBuilderFactory jwsBuilderFactory) {
        // We prepare access tokens with different authorizations to be used in the consumer tests
        SemanticApplicationRole semanticApplicationRole = SemanticApplicationRole.builder()
                .system("jme")
                .resource("task")
                .operation("read")
                .build();
        taskReadToken = jwsBuilderFactory.createValidForFixedLongPeriodBuilder("taskClient", JeapAuthenticationContext.SYS)
                .withUserRoles(semanticApplicationRole)
                .build().serialize();
        unrelatedRoleToken = jwsBuilderFactory.createValidForFixedLongPeriodBuilder("taskClient", JeapAuthenticationContext.SYS).
                withUserRoles("some-unrelated-role").
                build().serialize();
    }

    // A @Pact annotated method specifies an interaction between a consumer and its provider using the Pact DSL.
    // In the interaction specified below the provider is asked for a task with task id '1' and then provides that task to
    // the consumer. A known task id is needed in this interaction because the id is a required part of the path used to
    // fetch the task from the provider's REST endpoint. The existence of a task wit id '1' is a precondition for the specified
    // interaction, i.e. the provider is required to be in the state "A task with task id '1' is present" before the interaction
    // can take place.
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    private V4Pact requestTaskWithFixedIdTaskBeingPresentInteraction(PactBuilder builder) {
        final String path = API_PATH + "/1";
        // @formatter:off
        return builder.usingLegacyDsl().given("A task with task id '1' is present").
                uponReceiving("A GET request to " + path).
                path(path).
                method("GET").
                headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).
                headers(HttpHeaders.AUTHORIZATION, "Bearer " + taskReadToken).
                willRespondWith().
                status(200).
                headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).
                body(new PactDslJsonBody().
                        stringValue(ID_FIELD_NAME, "1").
                        stringType(TITLE_FIELD_NAME, TITLE_EXAMPLE_VALUE).
                        stringType(CONTENT_FIELD_NAME, CONTENT_EXAMPLE_VALUE).
                        asBody()).
                toPact(V4Pact.class);
        // @formatter:on
    }

    // A @PactTest annotated method executes a single consumer Pact test for the interaction defined by the referenced pact method.
    // The test will start a mock of the provider on port 8888. The mock provider will behave as defined by the pact method. The test
    // will add the interaction specification to a generated file representation of this consumer's pact with the provider. The pact file
    // can then be uploaded to the Pact broker.
    @Test
    @PactTestFor(pactMethod = "requestTaskWithFixedIdTaskBeingPresentInteraction")
    void testGetTaskByIdWithFixedIdTaskBeingPresent() {
        mockRestClientBuilderFactory.getAuthTokenProvider().setAuthToken(taskReadToken);

        Task result = taskClient.getTaskById("1");

        assertTaskValues(result, "1",TITLE_EXAMPLE_VALUE, CONTENT_EXAMPLE_VALUE);
    }


    // The task provider might not be able to create a task with a fixed task id to be used for a provider state, e.g. because
    // the IDs of tasks are created by a database sequence. Fortunately it is possible with Pact to dynamically assign parameters like
    // e.g. 'task-id' to a provider state and reference those parameters in the pact e.g. within the method pathFromProviderState()
    // as '${task-id}'. The consumer side Pact test will replace the parameter 'task-id' with the given example values. For the
    // provider side Pact test, the parameter must be set by the method that creates the provider state.
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    private V4Pact requestTaskWithTaskBeingPresentInteraction(PactBuilder builder) {
        final String taskIdParamExpression = "${" + TASK_ID_PARAM_NAME + "}";
        final String path = API_PATH + "/" + taskIdParamExpression;
        // @formatter:off
        return builder.usingLegacyDsl().given("A task is present").
                uponReceiving("A request to " + path).
                pathFromProviderState(path, API_PATH + "/" + ID_EXAMPLE_VALUE).
                method("GET").
                headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).
                headers(HttpHeaders.AUTHORIZATION, "Bearer " + taskReadToken).
                willRespondWith().
                status(200).
                headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).
                body(new PactDslJsonBody().
                        valueFromProviderState(ID_FIELD_NAME, taskIdParamExpression , ID_EXAMPLE_VALUE).
                        stringType(TITLE_FIELD_NAME, "test-title").
                        stringType(CONTENT_FIELD_NAME, "test-content").
                        asBody()).
                toPact(V4Pact.class);
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "requestTaskWithTaskBeingPresentInteraction")
    void testGetTaskByIdWithTaskBeingPresent() {
        mockRestClientBuilderFactory.getAuthTokenProvider().setAuthToken(taskReadToken);

        Task result = taskClient.getTaskById(ID_EXAMPLE_VALUE);

        assertTaskValues(result, ID_EXAMPLE_VALUE, TITLE_EXAMPLE_VALUE, CONTENT_EXAMPLE_VALUE);
    }


    // In the previous interaction definition 'requestTaskWithTaskBeingPresentInteraction()' provider state parameters were
    // set by the provider. Provider state parameters can also be set by the consumer as part of the interaction definition
    // in the given(..) clause. Those parameters become then a part of the pact. On the provider side, the methods that set up
    // provider states containing such parameter definitions will receive those definitions as a parameter map and can use
    // the parameter values for the state set-up. This makes it possible to specify provider states that are to some
    // extent generic, if needed. In the real world, there would be no need for this in our example, though.
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    private V4Pact requestTaskWithGivenTaskIdBeingPresentInteraction(PactBuilder builder) {
        final String taskIdParamExpression = "${" + TASK_ID_PARAM_NAME + "}";
        // @formatter:off
        return builder.usingLegacyDsl().given("A task with task id " + taskIdParamExpression + " is present", TASK_ID_PARAM_NAME, ID_EXAMPLE_VALUE).
                uponReceiving("A GET request to " +  API_PATH + "/" + taskIdParamExpression).
                path(API_PATH + "/" + ID_EXAMPLE_VALUE). // pathFromProviderState() does not seem to work with parameters set by the consumer
                method("GET").
                headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).
                headers(HttpHeaders.AUTHORIZATION, "Bearer " + taskReadToken).
                willRespondWith().
                status(200).
                headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).
                body(new PactDslJsonBody().
                        stringValue(ID_FIELD_NAME, ID_EXAMPLE_VALUE).
                        stringType(TITLE_FIELD_NAME, TITLE_EXAMPLE_VALUE).
                        stringType(CONTENT_FIELD_NAME, CONTENT_EXAMPLE_VALUE).
                        asBody()).
                toPact(V4Pact.class);
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "requestTaskWithGivenTaskIdBeingPresentInteraction")
    void testGetTaskByIdWithGivenTaskIdBeingPresent() {
        mockRestClientBuilderFactory.getAuthTokenProvider().setAuthToken(taskReadToken);

        Task result = taskClient.getTaskById(ID_EXAMPLE_VALUE);

        assertTaskValues(result, ID_EXAMPLE_VALUE,TITLE_EXAMPLE_VALUE, CONTENT_EXAMPLE_VALUE);
    }


    // The following interaction specification defines how insufficient authorization is expected to be handled.
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    private V4Pact requestTaskWithInsufficientAuthorizationInteraction(PactBuilder builder) {
        final String path = API_PATH + "/1";
        // @formatter:off
        return builder.usingLegacyDsl().given("A task with task id '1' is present").
                uponReceiving("A request to " + path + " with insufficient authorization").
                path(path).
                method("GET").
                headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).
                headers(HttpHeaders.AUTHORIZATION, "Bearer " + unrelatedRoleToken).
                willRespondWith().
                status(403).
                toPact(V4Pact.class);
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "requestTaskWithInsufficientAuthorizationInteraction")
    void testGetTaskInsufficientAuthorization() {
        mockRestClientBuilderFactory.getAuthTokenProvider().setAuthToken(unrelatedRoleToken);

        assertThatExceptionOfType(AuthenticationException.class).isThrownBy( () -> taskClient.getTaskById("1"));
    }


    // This interaction specification gives an example of the Pact DSL for the case that the provider response is
    // an array instead of a JSON object as in the previous specifications.
    @Pact(provider = PROVIDER, consumer = CONSUMER)
    private V4Pact requestTasksWithTasksBeingPresentInteraction(PactBuilder builder) {
        // @formatter:off
        return builder.usingLegacyDsl().given("Several tasks are present").
                uponReceiving("A request to " + API_PATH).
                path(API_PATH).
                method("GET").
                headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).
                headers(HttpHeaders.AUTHORIZATION, "Bearer " + taskReadToken).
                willRespondWith().
                status(200).
                headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).
                body(PactDslJsonArray.arrayMinLike(2, 7)
                        .stringType(ID_FIELD_NAME, ID_EXAMPLE_VALUE)
                        .stringType(TITLE_FIELD_NAME, TITLE_EXAMPLE_VALUE)
                        .stringType(CONTENT_FIELD_NAME, CONTENT_EXAMPLE_VALUE)
                        .closeObject()).
                toPact(V4Pact.class);
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "requestTasksWithTasksBeingPresentInteraction")
    void testGetAllTasksWithTasksBeingPresent() {
        mockRestClientBuilderFactory.getAuthTokenProvider().setAuthToken(taskReadToken);

        List<Task> tasks = taskClient.getAllTasks();

        assertThat(tasks).hasSize(7);
        tasks.forEach(
                task -> assertTaskValues(task, ID_EXAMPLE_VALUE, TITLE_EXAMPLE_VALUE, CONTENT_EXAMPLE_VALUE)
        );
    }

    // Additional interactions should be specified here...


    private void assertTaskValues(Task task, String id, String title, String content) {
        assertThat(task.getId()).isEqualTo(id);
        assertThat(task.getTitle()).isEqualTo(title);
        assertThat(task.getContent()).isEqualTo(content);
    }
}

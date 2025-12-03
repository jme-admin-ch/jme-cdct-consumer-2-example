package ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class TaskClient {

    private final RestClient restClient;

    public TaskClient(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory,
                      @Value("${jme.cdct-consumer-2.task-api-url}") String taskApiUrl) {
        this.restClient = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId("jme-cdct-consumer-service").baseUrl(taskApiUrl).build();
    }

    public Task getTaskById(String id) {
        return restClient.get().
                uri("/{id}", id).
                accept(APPLICATION_JSON).
                retrieve().
                onStatus(status -> status.isSameCodeAs(HttpStatus.FORBIDDEN), (request, response) -> {
                    throw new InsufficientAuthenticationException("Insufficient authentication to access task API.");
                }).
                body(Task.class);
    }

    @SuppressWarnings("java:S2583") // SonarQube seems to get this wrong, block can return null if the Mono is empty.
    public List<Task> getAllTasks() {
        Task[] taskArray = restClient.get().
                accept(APPLICATION_JSON).
                retrieve().
                body(Task[].class);
        if (taskArray != null) {
            return asList(taskArray);
        }
        else {
            return emptyList();
        }
    }

}

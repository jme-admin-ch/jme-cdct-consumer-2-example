package ch.admin.bit.jeap.jme.cdct.consumer.web.api;

import ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task.Task;
import ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task.TaskClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
class TaskGatewayController {

    private final TaskClient taskClient;

    @GetMapping()
    public List<Task> getAllTasks() {
        return taskClient.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable("id") String id) {
        return taskClient.getTaskById(id);
    }

}

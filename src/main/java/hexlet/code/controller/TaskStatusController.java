package hexlet.code.controller;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + TASK_STATUS_CONTROLLER_PATH)
@RequiredArgsConstructor
public class TaskStatusController {
    public static final String TASK_STATUS_CONTROLLER_PATH = "/statuses";
    public static final String ID = "/{id}";

    private static final String ONLY_TASK_STATUS_OWNER_BY_ID = """
            @taskStatusRepository.findById(#id).get().getCreatedBy() == authentication.getName()
        """;

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusService taskStatusService;

    @PostMapping(path = "")
    @ResponseStatus(CREATED)
    @PreAuthorize("isAuthenticated()")
    public TaskStatus createNewTaskStatus(@RequestBody @Valid final TaskStatusDto taskStatusDto) {
        return taskStatusService.createNewTaskStatus(taskStatusDto);
    }

    @GetMapping(path = ID)
    public TaskStatus getTaskStatusById(@PathVariable final Long id) {
        return taskStatusRepository.findById(id).get();
    }

    @GetMapping(path = "")
    public List<TaskStatus> getAllTaskStatuses() {
        return taskStatusRepository.findAll()
                .stream()
                .toList();
    }

    @PutMapping(path = ID)
    @PreAuthorize("isAuthenticated()")
    public TaskStatus updateTaskStatus(@PathVariable final long id,
                                       @RequestBody @Valid final TaskStatusDto taskStatusDto) {
        return taskStatusService.updateTaskStatus(id, taskStatusDto);
    }

    @DeleteMapping(path = ID)
    @PreAuthorize("isAuthenticated()")
    public void deleteTaskStatus(@PathVariable final long id) {
        taskStatusRepository.deleteById(id);
    }
}

package hexlet.code.controller;

import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
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

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + TASK_CONTROLLER_PATH)
@RequiredArgsConstructor
public class TaskController {
    public static final String TASK_CONTROLLER_PATH = "/tasks";
    public static final String ID = "/{id}";

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    private static final String TASK_CREATOR =
            "@taskRepository.findById(#id).get().getAuthor().getEmail() == authentication.getName()";

    @PostMapping(path = "")
    @ResponseStatus(CREATED)
    public Task createNewTask(@RequestBody @Valid final TaskDto taskDto) {
        return taskService.createNewTask(taskDto);
    }

    @GetMapping(ID)
    public Task getTaskByID(@PathVariable long id) {
        return taskRepository.findById(id).get();
    }

    @GetMapping(path = "")
    public List<Task> getAllTaskStatuses() {
        return taskRepository.findAll()
                .stream()
                .toList();
    }

    @PutMapping(ID)
    public Task updateTask(@RequestBody @Valid final TaskDto taskDto, @PathVariable long id) {
        return taskService.updateTask(id, taskDto);
    }

    @DeleteMapping(ID)
    @PreAuthorize(TASK_CREATOR)
    public void deleteTask(@PathVariable final Long id) {
        taskRepository.deleteById(id);
    }
}

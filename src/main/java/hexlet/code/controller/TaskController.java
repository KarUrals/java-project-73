package hexlet.code.controller;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Task;
import hexlet.code.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + TASK_CONTROLLER_PATH)
@RequiredArgsConstructor
public class TaskController {
    public static final String TASK_CONTROLLER_PATH = "/tasks";
    public static final String ID = "/{id}";
    private static final String AUTHORIZED_USERS_ONLY = "isAuthenticated()";
    private static final String TASK_CREATOR =
            "@taskRepository.findById(#id).get().getAuthor().getEmail() == authentication.getName()";

    private final TaskService taskService;

    @Operation(summary = "Create new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping("/")
    @ResponseStatus(CREATED)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public Task createNewTask(
            @Parameter(description = "Task to save", schema = @Schema(implementation = TaskDto.class))
            @RequestBody @Valid final TaskDto taskDto) {
        return taskService.createNewTask(taskDto);
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "404", description = "Task with that ID not found")
    })
    @GetMapping(path = ID)
    public Task getTaskByID(
            @Parameter(description = "ID of task to find")
            @PathVariable long id) {
        return taskService.getTaskById(id);
    }

    @Operation(summary = "Get list of all tasks")
    @ApiResponse(responseCode = "200", description = "List of all tasks",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = Task.class)))
    @GetMapping("/")
    public Iterable<Task> getFilteredTasks(
            @Parameter(description = "Filtering options", hidden = true)
            @QuerydslPredicate(root = Task.class) Predicate predicate) {
        return predicate == null ? taskService.getAllTasks() : taskService.getAllTasks(predicate);
    }

    @Operation(summary = "Update existing task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "404", description = "Task with that ID not found"),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PutMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public Task updateTask(
            @Parameter(description = "ID of task to update")
            @PathVariable long id,
            @Parameter(description = "Task to update", schema = @Schema(implementation = TaskDto.class))
            @RequestBody @Valid final TaskDto taskDto) {
        return taskService.updateTask(id, taskDto);
    }

    @Operation(summary = "Delete task by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task with that ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(path = ID)
    @PreAuthorize(TASK_CREATOR)
    public void deleteTask(
            @Parameter(description = "ID of task to delete")
            @PathVariable final Long id) {
        taskService.deleteTaskById(id);
    }
}

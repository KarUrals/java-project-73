package hexlet.code.controller;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;
import hexlet.code.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private static final String AUTHORIZED_USERS_ONLY = "isAuthenticated()";

    private final TaskStatusService taskStatusService;

    @Operation(summary = "Create new task status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task status created",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = TaskStatus.class))),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping(path = "")
    @ResponseStatus(CREATED)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public TaskStatus createNewTaskStatus(
            @Parameter(description = "Task status to save", schema = @Schema(implementation = TaskStatusDto.class))
            @RequestBody @Valid final TaskStatusDto taskStatusDto) {
        return taskStatusService.createNewTaskStatus(taskStatusDto);
    }

    @Operation(summary = "Get task status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status found",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = TaskStatus.class))),
            @ApiResponse(responseCode = "404", description = "Task status with that ID not found")
    })
    @GetMapping(path = ID)
    public TaskStatus getTaskStatusById(
            @Parameter(description = "ID of task status to find")
            @PathVariable final Long id) {
        return taskStatusService.getTaskStatusById(id);
    }

    @Operation(summary = "Get list of all task statuses")
    @ApiResponse(responseCode = "200", description = "List of all task statuses",
            content = @Content(mediaType = "application/json",
                               schema = @Schema(implementation = TaskStatus.class)))
    @GetMapping(path = "")
    public List<TaskStatus> getAllTaskStatuses() {
        return taskStatusService.getAllTaskStatuses();
    }

    @Operation(summary = "Update existing task status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = TaskStatus.class))),
            @ApiResponse(responseCode = "404", description = "Task status with that ID not found"),
            @ApiResponse(responseCode = "422", description = "Request contains invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PutMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public TaskStatus updateTaskStatus(
            @Parameter(description = "ID of task status to update")
            @PathVariable final long id,
            @Parameter(description = "Task status to update", schema = @Schema(implementation = TaskStatusDto.class))
            @RequestBody @Valid final TaskStatusDto taskStatusDto) {
        return taskStatusService.updateTaskStatusById(id, taskStatusDto);
    }

    @Operation(summary = "Delete task status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status deleted"),
            @ApiResponse(responseCode = "404", description = "Task status with that ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @DeleteMapping(path = ID)
    @PreAuthorize(AUTHORIZED_USERS_ONLY)
    public void deleteTaskStatus(
            @Parameter(description = "ID of task status to delete")
            @PathVariable final long id) {
        taskStatusService.deleteTaskStatusById(id);
    }
}

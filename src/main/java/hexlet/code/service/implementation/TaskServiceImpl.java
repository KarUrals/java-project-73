package hexlet.code.service.impl;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Label;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
import hexlet.code.entity.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final UserService userService;
    private final TaskStatusService taskStatusService;
    private final LabelService labelService;

    @Override
    public Task createNewTask(TaskDto taskDto) {
        final Task task = createTaskFromDto(taskDto);
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(long id) {
        return taskRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Iterable<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Iterable<Task> getAllTasks(Predicate predicate) {
        return taskRepository.findAll(predicate);
    }

    @Override
    public Task updateTask(long id, TaskDto taskDto) {
        final Task taskToUpdate = createTaskFromDto(taskDto);
        taskToUpdate.setId(id);
        taskToUpdate.setAuthor(getTaskById(id).getAuthor());
        taskToUpdate.setCreatedAt(getTaskById(id).getCreatedAt());
        return taskRepository.save(taskToUpdate);
    }

    @Override
    public void deleteTaskById(long id) {
        getTaskById(id);
        taskRepository.deleteById(id);
    }

    private Task createTaskFromDto(final TaskDto taskDto) {
        final User author = userService.getCurrentUser();
        final User executor = Optional.ofNullable(taskDto.getExecutorId())
                .flatMap(userId -> Optional.ofNullable(userService.getUserById(userId)))
                .orElse(null);
        final TaskStatus taskStatus = Optional.ofNullable(taskDto.getTaskStatusId())
                .flatMap(taskStatusId -> Optional.ofNullable(taskStatusService.getTaskStatusById(taskStatusId)))
                .orElse(null);
        final Set<Label> labels = Optional.ofNullable(taskDto.getLabelIds())
                .map(labelIds -> labelIds.stream()
                        .map(labelService::getLabelById)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(taskStatus)
                .labels(labels)
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .build();
    }
}

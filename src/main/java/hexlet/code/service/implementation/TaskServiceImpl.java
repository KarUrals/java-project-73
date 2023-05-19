package hexlet.code.service.implementation;

import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
import hexlet.code.entity.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskService;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    @Override
    public Task createNewTask(TaskDto taskDto) {
        final Task task = createTaskFromDto(taskDto);
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(long id, TaskDto taskDto) {
        final Task taskToUpdate = createTaskFromDto(taskDto);
        taskToUpdate.setId(id);
        return taskRepository.save(taskToUpdate);
    }

    private Task createTaskFromDto(final TaskDto taskDto) {
        final User author = userService.getCurrentUser();
        final User executor = userRepository.findById(taskDto.getExecutorId()).get();
        final TaskStatus taskStatus = taskStatusRepository.findById(taskDto.getTaskStatusId()).get();

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(taskStatus)
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .build();
    }
}

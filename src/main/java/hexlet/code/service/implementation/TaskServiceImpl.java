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

import java.util.Optional;

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
//        final Task task = fromDto(taskDto);
        final Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setTaskStatus(taskStatusRepository.findById(taskDto.getTaskStatusId()).get());
        task.setAuthor(userService.getCurrentUser());
        task.setExecutor(userRepository.findById(taskDto.getExecutorId()).get());
        task.setName(taskDto.getName());
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(long id, TaskDto taskDto) {
//        final Task taskToUpdate = fromDto(taskDto);
//        taskToUpdate.setId(id);
        final Task taskToUpdate = taskRepository.findById(id).get();
        taskToUpdate.setName(taskDto.getName());
        taskToUpdate.setDescription(taskDto.getDescription());
        taskToUpdate.setTaskStatus(taskStatusRepository.findById(taskDto.getTaskStatusId()).get());
        taskToUpdate.setAuthor(userService.getCurrentUser());
        taskToUpdate.setExecutor(userRepository.findById(taskDto.getExecutorId()).get());
        return taskRepository.save(taskToUpdate);
    }

    private Task fromDto(final TaskDto taskDto) {
        final User author = userService.getCurrentUser();
        final User executor = Optional.ofNullable(taskDto.getExecutorId())
                .map(User::new)
                .orElse(null);
        final TaskStatus taskStatus = Optional.ofNullable(taskDto.getTaskStatusId())
                .map(TaskStatus::new)
                .orElse(null);

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(taskStatus)
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .build();
    }
}

package hexlet.code.service.implementation;

import hexlet.code.AlreadyExistsException;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    @Override
    public TaskStatus createNewTaskStatus(TaskStatusDto taskStatusDto) {
        final Optional<TaskStatus> existingTaskStatus = taskStatusRepository.findByName(taskStatusDto.getName());
        if (existingTaskStatus.isPresent()) {
            throw new AlreadyExistsException("Task status with name \""
                                            + taskStatusDto.getName()
                                            + "\" already exists");
        } else {
            final TaskStatus taskStatus = new TaskStatus();
            taskStatus.setName(taskStatusDto.getName());
            return taskStatusRepository.save(taskStatus);
        }
    }

    @Override
    public TaskStatus updateTaskStatus(Long id, TaskStatusDto taskStatusDto) {
        TaskStatus taskStatusToUpdate = taskStatusRepository.getById(id);
        taskStatusToUpdate.setName(taskStatusDto.getName());
        return taskStatusRepository.save(taskStatusToUpdate);
    }
}

package hexlet.code.service.impl;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    @Override
    public TaskStatus createNewTaskStatus(TaskStatusDto taskStatusDto) {
        final TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(taskStatusDto.getName());
        return taskStatusRepository.save(taskStatus);
    }

    @Override
    public TaskStatus getTaskStatusById(long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<TaskStatus> getAllTaskStatuses() {
        return taskStatusRepository.findAll();
    }

    @Override
    public TaskStatus updateTaskStatusById(long id, TaskStatusDto taskStatusDto) {
        final TaskStatus taskStatusToUpdate = getTaskStatusById(id);
        taskStatusToUpdate.setName(taskStatusDto.getName());
        return taskStatusRepository.save(taskStatusToUpdate);
    }

    @Override
    public void deleteTaskStatusById(long id) {
        getTaskStatusById(id);
        taskStatusRepository.deleteById(id);
    }
}

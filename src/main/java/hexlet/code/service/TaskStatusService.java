package hexlet.code.service;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;

import java.util.List;

public interface TaskStatusService {
    TaskStatus createNewTaskStatus(TaskStatusDto taskStatusDto);
    TaskStatus getTaskStatusById(long id);
    List<TaskStatus> getAllTaskStatuses();
    TaskStatus updateTaskStatusById(long id, TaskStatusDto taskStatusDto);
    void deleteTaskStatusById(long id);
}

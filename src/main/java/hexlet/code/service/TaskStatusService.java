package hexlet.code.service;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.entity.TaskStatus;

public interface TaskStatusService {
    TaskStatus createNewTaskStatus(TaskStatusDto taskStatusDto);
    TaskStatus updateTaskStatus(long id, TaskStatusDto taskStatusDto);
}

package hexlet.code.service;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Task;

public interface TaskService {
    Task createNewTask(TaskDto taskDto);
    Task getTaskById(long id);
    Iterable<Task> getAllTasks(Predicate predicate);
    Task updateTask(long id, TaskDto taskDto);
    void deleteTaskById(long id);
}

package pe.edu.upc.managewise.backend.task.domain.services;

import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.managewise.backend.task.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface TaskQueryService {
    List<Task> handle(GetAllTasksQuery query);
    Optional<Task> handle(GetTaskByIdQuery query);
    List<Task> handle(GetTasksByProjectIdQuery query);
    List<Task> handle(GetTasksByAssignedUserIdQuery query);
    List<Task> handle(GetTasksByProjectIdAndAssignedUserIdQuery query);
    List<Task> handle(GetTasksByStatusQuery query);
    List<Task> handle(GetTasksByPriorityQuery query);
}

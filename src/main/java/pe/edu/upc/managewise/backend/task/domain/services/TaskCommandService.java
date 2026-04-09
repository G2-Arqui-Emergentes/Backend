package pe.edu.upc.managewise.backend.task.domain.services;

import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.managewise.backend.task.domain.model.commands.*;

import java.util.Optional;

public interface TaskCommandService {
    Optional<Task> handle(CreateTaskCommand command);
    Optional<Task> handle(UpdateTaskCommand command);
    Optional<Task> handle(UpdateTaskStatusCommand command);
    Optional<Task> handle(AssignUserToTaskCommand command);
    Optional<Task> handle(DeleteTaskCommand command);
    void handle(RemoveUserFromTaskCommand command);
}

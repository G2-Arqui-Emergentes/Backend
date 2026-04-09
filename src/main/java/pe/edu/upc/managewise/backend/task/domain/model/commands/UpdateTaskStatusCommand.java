package pe.edu.upc.managewise.backend.task.domain.model.commands;

import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;

public record UpdateTaskStatusCommand(
        Long taskId,
        Status status
) {
    public UpdateTaskStatusCommand {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID cannot be empty or negative");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}

package pe.edu.upc.managewise.backend.task.domain.model.commands;

public record DeleteTaskCommand(
        Long taskId
) {
    public DeleteTaskCommand {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID cannot be empty or negative");
        }
    }
}

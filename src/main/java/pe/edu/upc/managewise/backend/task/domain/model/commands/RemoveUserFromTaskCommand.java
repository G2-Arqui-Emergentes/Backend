package pe.edu.upc.managewise.backend.task.domain.model.commands;

public record RemoveUserFromTaskCommand(
        Long taskId,
        Long userId
) {
    public RemoveUserFromTaskCommand {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID cannot be empty or negative");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be empty or negative");
        }
    }
}

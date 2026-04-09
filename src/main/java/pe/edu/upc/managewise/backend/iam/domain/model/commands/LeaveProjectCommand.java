package pe.edu.upc.managewise.backend.iam.domain.model.commands;

public record LeaveProjectCommand(Long userId, Long projectId) {
    public LeaveProjectCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId cannot be empty or negative");
        }
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("ProjectId cannot be empty or negative");
        }
    }
}

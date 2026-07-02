package pe.edu.upc.taskmaster.backend.task.domain.model.queries;

public record GetTasksByAssignedUserIdQuery(
        Long userId
) {
    public GetTasksByAssignedUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be empty or negative");
        }
    }
}

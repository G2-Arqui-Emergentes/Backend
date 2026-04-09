package pe.edu.upc.managewise.backend.task.domain.model.queries;

public record GetTasksByProjectIdAndAssignedUserIdQuery(
        Long projectId,
        Long userId
) {
    public GetTasksByProjectIdAndAssignedUserIdQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be empty or negative");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be empty or negative");
        }
    }
}

package pe.edu.upc.managewise.backend.task.domain.model.queries;

public record GetTasksByProjectIdQuery(
        Long projectId
) {
    public GetTasksByProjectIdQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be empty or negative");
        }
    }
}

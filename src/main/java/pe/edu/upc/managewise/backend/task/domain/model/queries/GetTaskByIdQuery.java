package pe.edu.upc.managewise.backend.task.domain.model.queries;

public record GetTaskByIdQuery(
        Long taskId
) {
    public GetTaskByIdQuery {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID cannot be empty or negative");
        }
    }
}

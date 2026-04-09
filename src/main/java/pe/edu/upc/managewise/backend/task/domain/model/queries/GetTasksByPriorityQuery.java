package pe.edu.upc.managewise.backend.task.domain.model.queries;

import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;

public record GetTasksByPriorityQuery(
        Long projectId,
        Priority priority
) {
    public GetTasksByPriorityQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be empty or negative");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
    }
}

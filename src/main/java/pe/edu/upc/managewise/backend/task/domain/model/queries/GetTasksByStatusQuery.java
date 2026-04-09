package pe.edu.upc.managewise.backend.task.domain.model.queries;

import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;

public record GetTasksByStatusQuery(
        Long projectId,
        Status status
) {
    public GetTasksByStatusQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be empty or negative");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}

package pe.edu.upc.managewise.backend.project.domain.model.queries;

public record GetProjectCodeByIdQuery(
        Long projectId
) {
    public GetProjectCodeByIdQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
    }
}
